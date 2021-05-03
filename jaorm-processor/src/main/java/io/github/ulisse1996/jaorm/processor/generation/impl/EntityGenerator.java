package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.entity.ColumnGetter;
import io.github.ulisse1996.jaorm.entity.ColumnSetter;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.entity.converter.ParameterConverter;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.util.ReturnTypeDefinition;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.NoType;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityGenerator extends Generator {

    private static final String COL_NAME = "colName";
    private static final String REQUIRE_NON_NULL = "$T.requireNonNull(this.entity)";
    private static final String BUILDER_INSTANCE = "$T builder = new $T()";
    private static final String BUILDER_SIMPLE_APPEND = "builder.append($S)";
    private static final String KEYS_WHERE = "KEYS_WHERE";
    private static final String MAP_FORMAT = "$T values = new $T<>()";
    private static final JaormLogger logger = JaormLogger.getLogger(EntityGenerator.class);

    public EntityGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<GeneratedFile> types = roundEnvironment.getElementsAnnotatedWith(Table.class)
                .stream()
                .map(TypeElement.class::cast)
                .map(this::generate)
                .collect(Collectors.toList());
        types.forEach(f -> ProcessorUtils.generate(processingEnvironment, f));
        if (!types.isEmpty()) {
            generateDelegates(types);
        }
    }

    private void generateDelegates(List<GeneratedFile> types) {
        TypeSpec delegates = TypeSpec.classBuilder("Delegates")
                .addModifiers(Modifier.PUBLIC)
                .superclass(DelegatesService.class)
                .addField(delegatesMap(), "delegates", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(delegateConstructor(types))
                .addMethod(buildGetDelegates())
                .build();
        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, delegates, ""));
    }

    private MethodSpec buildGetDelegates() {
        return MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getDelegates", DelegatesService.class))
                .addStatement("return this.delegates")
                .build();
    }

    private ParameterizedTypeName delegatesMap() {
        return ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),
                ParameterizedTypeName.get(ClassName.get(Supplier.class), WildcardTypeName.subtypeOf(
                        ParameterizedTypeName.get(ClassName.get(EntityDelegate.class), WildcardTypeName.subtypeOf(Object.class))
                ))
        );
    }

    private MethodSpec delegateConstructor(List<GeneratedFile> types) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement(MAP_FORMAT, delegatesMap(), HashMap.class);
        types.forEach(type -> builder.addStatement("values.put($L.class, $LDelegate::new)",
                type.getEntityName(), type.getEntityName()));
        builder.addStatement("this.delegates = values");
        return builder.build();
    }

    private GeneratedFile generate(TypeElement entity) {
        logger.debug(() -> "Generating Delegate for Entity " + entity);
        String delegateName = entity.getSimpleName() + "Delegate";
        TypeSpec.Builder builder = TypeSpec.classBuilder(ClassName.get(getPackage(entity), delegateName))
                .addModifiers(Modifier.PUBLIC)
                .superclass(entity.asType())
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(EntityDelegate.class), ClassName.get(entity)));
        TypeSpec columns = generateColumns(processingEnvironment, entity);
        builder.addType(columns);
        builder.addField(ClassName.get(entity), "entity", Modifier.PRIVATE);
        builder.addField(addTableName(entity));
        builder.addField(addBaseSql(entity));
        builder.addField(addKeysWhere());
        builder.addField(addInsertSql());
        builder.addField(addUpdateSql());
        builder.addField(addDeleteSql());
        builder.addField(boolean.class, "modified", Modifier.PRIVATE);
        builder.addMethods(buildDelegation(entity));
        builder.addMethods(buildOverrideEntity(entity));
        return new GeneratedFile(getPackage(entity), builder.build(), entity.getQualifiedName().toString());
    }

    private Iterable<MethodSpec> buildDelegation(TypeElement entity) {
        List<? extends Element> elements = ProcessorUtils.getAllValidElements(processingEnvironment, entity);
        List<? extends ExecutableElement> methods = elements.stream()
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .filter(e -> !e.getKind().equals(ElementKind.CONSTRUCTOR))
                .collect(Collectors.toList());
        List<Map.Entry<Element, ExecutableElement>> joins = new ArrayList<>();
        for (Element element : elements) {
            if (hasJoinAnnotation(element)) {
                ExecutableElement getter = ProcessorUtils.findGetter(processingEnvironment,
                        entity, element.getSimpleName());
                joins.add(new AbstractMap.SimpleImmutableEntry<>(element, getter));
            }
        }

        List<MethodSpec> specs = new ArrayList<>();
        for (ExecutableElement m : methods) {
            Optional<Map.Entry<Element, ExecutableElement>> join = joins.stream()
                    .filter(p -> p.getValue().equals(m))
                    .findFirst();
            if (join.isPresent()) {
                specs.add(buildJoinMethod(entity, join.get()));
            } else {
                specs.add(buildDelegateMethod(m, entity));
            }
        }
        return specs;
    }

    private boolean hasJoinAnnotation(Element ele) {
        return ele.getAnnotation(Relationship.class) != null;
    }

    private MethodSpec buildDelegateMethod(ExecutableElement m, TypeElement entity) {
        MethodSpec.Builder builder = MethodSpec.overriding(m)
                .addStatement(REQUIRE_NON_NULL, Objects.class);
        String variables;
        variables = extractParameterNames(m);

        if (m.getSimpleName().contentEquals("equals")) {
            builder.addCode(buildCustomEquals(m.getParameters().get(0).getSimpleName().toString(), entity));
        } else if (m.getReturnType() instanceof NoType) {
            builder.addStatement("this.modified = true");
            builder.addStatement("this.entity.$L($L)", m.getSimpleName(), variables);
        } else {
            builder.addStatement("return this.entity.$L($L)", m.getSimpleName(), variables);
        }
        return builder.build();
    }

    private MethodSpec buildJoinMethod(TypeElement entity, Map.Entry<Element, ExecutableElement> join) {
        ExecutableElement method = join.getValue();
        ReturnTypeDefinition definition = new ReturnTypeDefinition(processingEnvironment, method.getReturnType());
        String runnerMethod;
        if (definition.isCollection()) {
            runnerMethod = "readAll";
        } else if (definition.isOptional()) {
            runnerMethod = "readOpt";
        } else {
            runnerMethod = "read";
        }

        Relationship relationship = join.getKey().getAnnotation(Relationship.class);
        Relationship.RelationshipColumn[] columns = relationship.columns();

        CodeBlock.Builder builder = CodeBlock.builder()
                .addStatement(REQUIRE_NON_NULL, Objects.class)
                .beginControlFlow("if (this.entity.$L() == null)", join.getValue().getSimpleName())
                .addStatement("$T params = new $T<>()", ParameterizedTypeName.get(List.class, SqlParameter.class), ArrayList.class);
        for (Relationship.RelationshipColumn column : columns) {
            VariableElement targetColumn = ProcessorUtils.getFieldWithColumnName(processingEnvironment,
                    definition.getRealClass(), column.targetColumn());
            buildJoinParam(column, builder, targetColumn, entity);
        }
        String wheres = createJoinWhere(columns);
        CodeBlock block = builder.addStatement("this.entity.$L($T.getInstance($T.class).$L($T.class, $T.getInstance().getSimpleSql($T.class) +  $S, params))",
                ProcessorUtils.findSetter(processingEnvironment, entity, join.getKey().getSimpleName()).getSimpleName(),
                QueryRunner.class, definition.getRealClass(), runnerMethod, definition.getRealClass(), DelegatesService.class,
                definition.getRealClass(), wheres)
            .endControlFlow()
            .addStatement("return this.entity.$L()", join.getValue().getSimpleName())
            .build();

        return MethodSpec.overriding(method)
                .addCode(block)
                .build();
    }

    private String createJoinWhere(Relationship.RelationshipColumn[] columns) {
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        for (Relationship.RelationshipColumn column : columns) {
            String target = column.targetColumn();
            if (first) {
                builder.append("WHERE ").append(target).append(" = ? ");
                first = false;
            } else {
                builder.append("AND ").append(target).append(" = ?");
            }
        }

        return " " + builder;
    }

    private void buildJoinParam(Relationship.RelationshipColumn column, CodeBlock.Builder builder,
                                VariableElement targetColumn,
                                TypeElement entity) {
        if (!column.sourceColumn().isEmpty()) {
            builder.addStatement("params.add(new $T($LDelegate.Column.findColumn($S).getGetter().apply(this.entity), $T.find($T.class).getSetter()))",
                    SqlParameter.class, entity.getSimpleName(), column.sourceColumn(), SqlAccessor.class, targetColumn.asType());
        } else {
            String defaultValue = column.defaultValue();
            builder.addStatement("params.add(new $T($T.$L.toValue($S), $T.find($T.$L.getKlass()).getSetter()))",
                    SqlParameter.class, ParameterConverter.class, column.converter(), defaultValue, SqlAccessor.class, ParameterConverter.class, column.converter());
        }
    }

    private CodeBlock buildCustomEquals(String paramName, TypeElement entity) {
        return CodeBlock.builder()
                .beginControlFlow("if (getClass().isInstance($L))", paramName)
                .addStatement("return this.entity.equals((($TDelegate) $L).entity)", entity, paramName)
                .endControlFlow()
                .addStatement("return this.entity.equals($L)", paramName)
                .build();
    }


    private FieldSpec addDeleteSql() {
        return FieldSpec.builder(String.class, "DELETE_SQL", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S + $L + $L", "DELETE ", "TABLE", KEYS_WHERE)
                .build();
    }

    private FieldSpec addUpdateSql() {
        return FieldSpec.builder(String.class, "UPDATE_SQL", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("Column.getUpdateSql()")
                .build();
    }

    private FieldSpec addInsertSql() {
        return FieldSpec.builder(String.class, "INSERT_SQL", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("Column.getInsertSql()")
                .build();
    }

    private FieldSpec addKeysWhere() {
        return FieldSpec.builder(String.class, KEYS_WHERE, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("Column.getKeyWheres()")
                .build();
    }

    private FieldSpec addBaseSql(TypeElement entity) {
        String select = "SELECT ";
        String from = " FROM " + entity.getAnnotation(Table.class).name();
        return FieldSpec.builder(String.class, "BASE_SQL", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S + Column.getSelectables() + $S", select, from)
                .build();
    }

    private FieldSpec addTableName(TypeElement entity) {
        return FieldSpec.builder(String.class, "TABLE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", entity.getAnnotation(Table.class).name())
                .build();
    }

    private Iterable<MethodSpec> buildOverrideEntity(TypeElement entity) {
        MethodSpec supplierEntity = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment,"getEntityInstance", EntityDelegate.class))
                .returns(ParameterizedTypeName.get(ClassName.get(Supplier.class), ClassName.get(entity)))
                .addStatement("return $T::new", entity)
                .build();
        MethodSpec entityMapper = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getEntityMapper", EntityDelegate.class))
                .returns(ParameterizedTypeName.get(ClassName.get(EntityMapper.class), ClassName.get(entity)))
                .addStatement("return Column.getEntityMapper()")
                .build();
        MethodSpec setEntity = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "setEntity", EntityDelegate.class))
                .addStatement("this.modified = false")
                .addStatement("this.entity = toEntity($L)", extractParameterNames(ProcessorUtils.getMethod(processingEnvironment, "setEntity", EntityDelegate.class)))
                .build();
        MethodSpec setEntityObj = MethodSpec.methodBuilder("setFullEntity")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(entity), "entity")
                .addStatement("this.entity = entity")
                .build();
        MethodSpec getEntity = MethodSpec.methodBuilder("getEntity")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(entity))
                .addStatement("return this.entity")
                .build();
        MethodSpec baseSql = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getBaseSql", EntityDelegate.class))
                .addStatement("return BASE_SQL")
                .build();
        MethodSpec keysWhere = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getKeysWhere", EntityDelegate.class))
                .addStatement("return KEYS_WHERE")
                .build();
        MethodSpec insertSql = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getInsertSql", EntityDelegate.class))
                .addStatement("return INSERT_SQL")
                .build();
        MethodSpec selectables = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getSelectables", EntityDelegate.class))
                .addStatement("return Column.getSelectables().replace($S,$S).replace($S,$S).split($S)", "(", "", ")", "", ",")
                .build();
        MethodSpec table = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getTable", EntityDelegate.class))
                .addStatement("return TABLE")
                .build();
        MethodSpec updateSql = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getUpdateSql", EntityDelegate.class))
                .addStatement("return UPDATE_SQL")
                .build();
        MethodSpec deleteSql = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getDeleteSql", EntityDelegate.class))
                .addStatement("return DELETE_SQL")
                .build();
        MethodSpec modified = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "isModified", EntityDelegate.class))
                .addStatement("return this.modified")
                .build();
        return Stream.of(supplierEntity, entityMapper,
                setEntity, setEntityObj, baseSql, keysWhere,
                insertSql, selectables, table, updateSql,
                getEntity, deleteSql, modified)
                .collect(Collectors.toList());
    }

    private String extractParameterNames(ExecutableElement m) {
        if (!m.getParameters().isEmpty()) {
            return m.getParameters().stream()
                    .map(VariableElement::getSimpleName)
                    .collect(Collectors.joining(","));
        }

        return "";
    }

    private TypeSpec generateColumns(ProcessingEnvironment processingEnvironment, TypeElement entity) {
        List<Element> columns = ProcessorUtils.getAnnotated(processingEnvironment, entity, Column.class);
        List<Accessor> accessors = columns.stream()
                .map(ele -> Accessor.of(processingEnvironment, ele))
                .collect(Collectors.toList());
        TypeSpec.Builder builder = TypeSpec.enumBuilder("Column");
        builder.addModifiers(Modifier.PUBLIC);
        builder.addField(getColumnGetterType(entity), "getter", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(getColumnSetterType(entity), "setter", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(getGenericClassType(), "type", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(String.class, COL_NAME, Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(boolean.class, "key", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(boolean.class, "autoGenerated", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(getEntityMapperType(entity), "ENTITY_MAPPER", Modifier.PRIVATE, Modifier.STATIC);
        builder.addField(String.class, "SELECTABLES", Modifier.PRIVATE, Modifier.STATIC);
        builder.addField(String.class, KEYS_WHERE, Modifier.PRIVATE, Modifier.STATIC);
        builder.addField(String.class, "INSERT_SQL", Modifier.PRIVATE, Modifier.STATIC);
        builder.addField(String.class, "UPDATE_SQL", Modifier.PRIVATE, Modifier.STATIC);
        builder.addMethod(addColumnConstructor(entity));
        for (Accessor accessor : accessors) {
            builder.addEnumConstant(accessor.name, enumImpl(accessor, entity));
        }
        addColumnsMethods(builder, entity);
        return builder.build();
    }

    private void addColumnsMethods(TypeSpec.Builder builder, TypeElement entity) {
        builder.addMethod(
                MethodSpec.methodBuilder("getGetter")
                        .returns(ParameterizedTypeName.get(ClassName.get(ColumnGetter.class), ClassName.get(entity), WildcardTypeName.subtypeOf(Object.class)))
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return this.getter")
                        .build()
        );
        builder.addMethod(
                MethodSpec.methodBuilder("getEntityMapper")
                        .returns(getEntityMapperType(entity))
                        .addModifiers(Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .addCode(buildEntityMapperCodeBlock(entity))
                        .build()
        );
        builder.addMethod(
                MethodSpec.methodBuilder("getSelectables")
                        .returns(String.class)
                        .addModifiers(Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .addCode(buildSelectablesCodeBlock())
                        .build()
        );
        builder.addMethod(
                MethodSpec.methodBuilder("getKeyWheres")
                        .returns(String.class)
                        .addModifiers(Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .addCode(buildKeysWhereCodeBlock())
                        .build()
        );
        builder.addMethod(
                MethodSpec.methodBuilder("getInsertSql")
                        .returns(String.class)
                        .addModifiers(Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .addCode(buildInsertSqlCodeBlock(entity.getAnnotation(Table.class).name()))
                        .build()
        );
        builder.addMethod(
                MethodSpec.methodBuilder("getUpdateSql")
                        .returns(String.class)
                        .addModifiers(Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .addCode(buildUpdateSql(entity.getAnnotation(Table.class).name()))
                        .build()
        );
        builder.addMethod(
                MethodSpec.methodBuilder("findColumn")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.bestGuess("Column"))
                        .addParameter(String.class, COL_NAME)
                        .addModifiers(Modifier.STATIC)
                        .addCode(buildFindColumnCodeBlock())
                        .build()
        );
    }

    private CodeBlock buildUpdateSql(String table) {
        return CodeBlock.builder()
                .beginControlFlow("if (UPDATE_SQL == null)")
                .addStatement(BUILDER_INSTANCE, StringBuilder.class, StringBuilder.class)
                .addStatement(BUILDER_SIMPLE_APPEND, "UPDATE " + table + " ")
                .addStatement("boolean first = true")
                .beginControlFlow("for (int i = 0; i < values().length; i++)")
                .addStatement("Column column = values()[i]")
                .beginControlFlow("if (first)")
                .addStatement(BUILDER_SIMPLE_APPEND, "SET ")
                .addStatement("first = false")
                .endControlFlow()
                .addStatement("builder.append(column.colName).append($S)", " = ?")
                .beginControlFlow("if (i != values().length - 1)")
                .addStatement(BUILDER_SIMPLE_APPEND, ",")
                .endControlFlow()
                .endControlFlow()
                .addStatement("UPDATE_SQL = builder.toString()")
                .endControlFlow()
                .addStatement("return UPDATE_SQL")
                .build();
    }

    private CodeBlock buildInsertSqlCodeBlock(String table) {
        return CodeBlock.builder()
                .beginControlFlow("if (INSERT_SQL == null)")
                .addStatement(BUILDER_INSTANCE, StringBuilder.class, StringBuilder.class)
                .addStatement(BUILDER_SIMPLE_APPEND, "INSERT INTO " + table + " (")
                .beginControlFlow("for (int i = 0; i < values().length; i++)")
                .addStatement("Column column = values()[i]")
                .beginControlFlow("if (column.autoGenerated)")
                .addStatement("continue")
                .endControlFlow()
                .addStatement("builder.append(column.colName)")
                .beginControlFlow("if (i != values().length - 1)")
                .addStatement(BUILDER_SIMPLE_APPEND, ",")
                .endControlFlow()
                .endControlFlow()
                .addStatement("String wildcards = Stream.of(values()).filter(c -> !c.autoGenerated).map(c -> $S).collect($T.joining($S))", "?", Collectors.class, ",")
                .addStatement("builder.append($S).append(wildcards).append($S)", ") VALUES (", ")")
                .addStatement("INSERT_SQL = builder.toString()")
                .endControlFlow()
                .addStatement("return INSERT_SQL")
                .build();
    }

    private CodeBlock buildFindColumnCodeBlock() {
        return CodeBlock.builder()
                .beginControlFlow("for (Column col : values())")
                .beginControlFlow("if (col.colName.equals(colName))")
                .addStatement("return col")
                .endControlFlow()
                .endControlFlow()
                .addStatement("throw new $T($S + colName)", IllegalArgumentException.class, "Can't find column ")
                .build();
    }

    private CodeBlock buildKeysWhereCodeBlock() {
        String where = " WHERE ";
        String wildcard = " = ? ";
        String and = " AND ";
        return CodeBlock.builder()
                .beginControlFlow("if (KEYS_WHERE == null)")
                .addStatement("$L keys = $T.of(values()).filter(col -> col.key).map(col -> col.colName).collect($T.toList())",
                        ParameterizedTypeName.get(List.class, String.class), Stream.class, Collectors.class)
                .addStatement("boolean first = true")
                .addStatement(BUILDER_INSTANCE, StringBuilder.class, StringBuilder.class)
                .beginControlFlow("for (String key : keys)")
                .beginControlFlow("if (first)")
                .addStatement("builder.append($S).append(key).append($S)", where, wildcard)
                .addStatement("first = false")
                .nextControlFlow("else")
                .addStatement("builder.append($S).append(key).append($S)", and, wildcard)
                .endControlFlow()
                .endControlFlow()
                .addStatement("KEYS_WHERE = builder.toString()")
                .endControlFlow()
                .addStatement("return KEYS_WHERE")
                .build();
    }

    private CodeBlock buildSelectablesCodeBlock() {
        return CodeBlock.builder()
                .beginControlFlow("if (SELECTABLES == null)")
                .addStatement("SELECTABLES = $T.of(values()).map(col -> col.colName).collect($T.joining($S))",
                        Stream.class, Collectors.class, ",")
                .endControlFlow()
                .addStatement("return SELECTABLES")
                .build();
    }

    private CodeBlock buildEntityMapperCodeBlock(TypeElement entity) {
        return CodeBlock.builder()
                .beginControlFlow("if (ENTITY_MAPPER == null)")
                .addStatement("$T.Builder<$T> builder = new $T.Builder<>()", EntityMapper.class, entity, EntityMapper.class)
                .beginControlFlow("for (Column col : Column.values())")
                .addStatement("builder.add(col.colName, col.type, col.setter, col.getter, col.key, col.autoGenerated)")
                .endControlFlow()
                .addStatement("ENTITY_MAPPER = builder.build()")
                .endControlFlow()
                .addStatement("return ENTITY_MAPPER")
                .build();
    }

    private TypeSpec enumImpl(Accessor accessor, TypeElement entity) {
        String converter = accessor.converterInstance;
        if (converter != null) {
            return TypeSpec.anonymousClassBuilder("$T.class, $S, entity -> $L.toSql(entity.$L()), (entity, val) -> entity.$L($L.fromSql(($T) val)), $L, $L",
                    accessor.beforeConverterClass,
                    accessor.name,
                    converter,
                    accessor.getter.getSimpleName(),
                    accessor.setter.getSimpleName(),
                    converter,
                    accessor.beforeConverterClass,
                    accessor.key,
                    accessor.autoGenerated
            ).build();
        } else {
            return TypeSpec.anonymousClassBuilder("$T.class, $S, $T::$L, (entity, val) -> entity.$L(($T) val), $L, $L",
                    accessor.getter.getReturnType(),
                    accessor.name,
                    ClassName.get(entity),
                    accessor.getter.getSimpleName(),
                    accessor.setter.getSimpleName(),
                    accessor.getter.getReturnType(),
                    accessor.key,
                    accessor.autoGenerated
            ).build();
        }
    }

    private MethodSpec addColumnConstructor(TypeElement entity) {
        return MethodSpec.constructorBuilder()
                .addParameter(getGenericClassType(), "type")
                .addParameter(String.class, COL_NAME)
                .addParameter(getColumnGetterType(entity), "getter")
                .addParameter(getColumnSetterType(entity), "setter")
                .addParameter(boolean.class, "key")
                .addParameter(boolean.class, "autoGenerated")
                .addStatement("this.getter = getter")
                .addStatement("this.setter = setter")
                .addStatement("this.colName = colName")
                .addStatement("this.type = type")
                .addStatement("this.key = key")
                .addStatement("this.autoGenerated = autoGenerated")
                .build();
    }

    private TypeName getEntityMapperType(TypeElement entity) {
        return ParameterizedTypeName.get(ClassName.get(EntityMapper.class), ClassName.get(entity));
    }

    private ParameterizedTypeName getGenericClassType() {
        return ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class));
    }

    private ParameterizedTypeName getColumnSetterType(TypeElement entity) {
        return ParameterizedTypeName.get(ClassName.get(ColumnSetter.class), TypeName.get(entity.asType()), ClassName.get(Object.class));
    }

    private ParameterizedTypeName getColumnGetterType(TypeElement entity) {
        return ParameterizedTypeName.get(ClassName.get(ColumnGetter.class), TypeName.get(entity.asType()), ClassName.get(Object.class));
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
    }

    private static class Accessor {

        private final String name;
        private final ExecutableElement getter;
        private final ExecutableElement setter;
        private final boolean key;
        private final String converterInstance;
        private final TypeElement beforeConverterClass;
        private final boolean autoGenerated;

        public Accessor(String name, ExecutableElement getter, ExecutableElement setter,
                        boolean key, String converterInstance, TypeElement beforeConverterClass, boolean autoGenerated) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.key = key;
            this.converterInstance = converterInstance;
            this.beforeConverterClass = beforeConverterClass;
            this.autoGenerated = autoGenerated;
        }

        private static Accessor of(ProcessingEnvironment processingEnvironment, Element element) {
            TypeElement entity = (TypeElement) element.getEnclosingElement();
            boolean key = element.getAnnotation(Id.class) != null;
            boolean autoGenerated = key && element.getAnnotation(Id.class).autoGenerated();
            Converter converter = element.getAnnotation(Converter.class);
            String converterInstance = null;
            TypeElement beforeConverterClass = null;
            ExecutableElement getter = ProcessorUtils.findGetter(processingEnvironment, entity, element.getSimpleName());
            ExecutableElement setter = ProcessorUtils.findSetter(processingEnvironment, entity, element.getSimpleName());
            if (converter != null) {
                converterInstance = ProcessorUtils.getConverterCaller(processingEnvironment, (VariableElement) element);
                beforeConverterClass = ProcessorUtils.getConverterTypes(processingEnvironment, (VariableElement) element)
                        .get(0);
            }
            return new Accessor(
                    element.getAnnotation(Column.class).name(),
                    getter,
                    setter,
                    key,
                    converterInstance,
                    beforeConverterClass,
                    autoGenerated
            );
        }
    }
}
