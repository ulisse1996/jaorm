package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.annotation.DefaultNumeric;
import io.github.ulisse1996.jaorm.annotation.DefaultString;
import io.github.ulisse1996.jaorm.annotation.DefaultTemporal;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Relationship;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.entity.ColumnGetter;
import io.github.ulisse1996.jaorm.entity.ColumnSetter;
import io.github.ulisse1996.jaorm.entity.DefaultGenerator;
import io.github.ulisse1996.jaorm.entity.DirtinessTracker;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.TrackedList;
import io.github.ulisse1996.jaorm.entity.converter.ParameterConverter;
import io.github.ulisse1996.jaorm.entity.relationship.LazyEntityInfo;
import io.github.ulisse1996.jaorm.entity.relationship.RelationshipManager;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.external.support.mock.MockGetter;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.util.ReturnTypeDefinition;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.GeneratorsService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityGenerator extends Generator {

    private static final List<Class<? extends Annotation>> DEFAULTS = Arrays.asList(
            DefaultNumeric.class,
            DefaultString.class,
            DefaultTemporal.class
    );
    private static final String COL_NAME = "colName";
    private static final String REQUIRE_NON_NULL = "$T.requireNonNull(this.entity)";
    private static final String BUILDER_INSTANCE = "$T builder = new $T()";
    private static final String BUILDER_SIMPLE_APPEND = "builder.append($S)";
    private static final String KEYS_WHERE = "KEYS_WHERE";
    private static final String WHERE = " WHERE ";
    private static final String WILDCARD = " = ? ";
    private static final String AND = " AND ";
    protected static final String RESET_FIRST = "first = false";
    protected static final String CHECK_FIRST = "if (first)";
    protected static final String SET_FIRST = "boolean first = true";
    protected static final String ENTITY = "entity";

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
        ProcessorUtils.generateSpi(processingEnvironment, types, EntityDelegate.class);
    }

    private GeneratedFile generate(TypeElement entity) {
        debugMessage("Generating Delegate for Entity " + entity);
        String delegateName = entity.getSimpleName() + "Delegate";
        TypeSpec.Builder builder = TypeSpec.classBuilder(ClassName.get(getPackage(entity), delegateName))
                .addModifiers(Modifier.PUBLIC)
                .superclass(entity.asType())
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(EntityDelegate.class), ClassName.get(entity)));

        TypeSpec columns = generateColumns(processingEnvironment, entity);
        builder.addType(columns);
        builder.addStaticBlock(buildEntityMapperGenerator(entity));
        builder.addStaticBlock(buildSelectablesGenerator());
        builder.addStaticBlock(buildKeysWhereGenerator());
        builder.addStaticBlock(buildInsertSqlGenerator(entity));
        builder.addStaticBlock(buildUpdateSqlGenerator(entity));

        builder.addField(ClassName.get(entity), ENTITY, Modifier.PRIVATE);
        builder.addField(
                FieldSpec.builder(
                            ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),
                            "entityClass",
                                Modifier.PRIVATE, Modifier.FINAL
                        )
                        .initializer("$L.class", ClassName.get(entity))
                        .build()
        );
        builder.addField(addTableName(entity));
        builder.addField(addSchemaName(entity));
        builder.addField(addSelectables());
        builder.addField(addKeysWhere());
        builder.addField(addInsertSql());
        builder.addField(addUpdateSql());
        builder.addField(addTracker(entity));
        builder.addField(addLazyEntityInfo());
        builder.addField(addRelationshipManager(entity));
        builder.addField(getEntityMapperType(entity), "ENTITY_MAPPER", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        builder.addField(TypeName.INT, "modifiedRow", Modifier.PRIVATE);
        builder.addField(boolean.class, "modified", Modifier.PRIVATE);
        DelegationInfo delegationInfo = buildDelegation(entity);
        builder.addMethods(delegationInfo.getMethods());
        builder.addMethods(buildOverrideEntity(entity));
        builder.addStaticBlock(getRelationshipManagerBlock(delegationInfo.getRelationshipsBlocks(), entity));
        return new GeneratedFile(getPackage(entity), builder.build(), entity.getQualifiedName().toString());
    }

    private FieldSpec addSelectables() {
        return FieldSpec.builder(String.class, "SELECTABLES", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .build();
    }

    private CodeBlock getRelationshipManagerBlock(List<CodeBlock> relationshipsBlocks, TypeElement entity) {
        ParameterizedTypeName type = ParameterizedTypeName.get(ClassName.get(RelationshipManager.class), ClassName.get(entity));
        CodeBlock.Builder builder = CodeBlock.builder()
                .addStatement("$T manager = new $T()", type, type);
        for (CodeBlock block : relationshipsBlocks) {
            builder.add(block);
        }
        builder.addStatement("RELATIONSHIP_MANAGER = manager");
        return builder.build();
    }

    private FieldSpec addRelationshipManager(TypeElement entity) {
        return FieldSpec.builder(
                ParameterizedTypeName.get(ClassName.get(RelationshipManager.class), ClassName.get(entity)),
                "RELATIONSHIP_MANAGER",
                Modifier.PRIVATE,
                Modifier.STATIC,
                Modifier.FINAL
        ).build();
    }

    private FieldSpec addTracker(TypeElement entity) {
        ParameterizedTypeName type = ParameterizedTypeName.get(ClassName.get(DirtinessTracker.class), ClassName.get(entity));
        return FieldSpec.builder(type, "tracker", Modifier.PRIVATE)
                .initializer("new $T(this)", type)
                .build();
    }

    private FieldSpec addLazyEntityInfo() {
        return FieldSpec.builder(LazyEntityInfo.class, "lazyInfo", Modifier.PRIVATE)
                .build();
    }

    private FieldSpec addSchemaName(TypeElement entity) {
        String schema = entity.getAnnotation(Table.class).schema();
        return FieldSpec.builder(String.class, "SCHEMA", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", schema)
                .build();
    }

    private DelegationInfo buildDelegation(TypeElement entity) {
        List<? extends Element> elements = ProcessorUtils.getAllValidElements(processingEnvironment, entity);
        List<ExecutableElement> methods = ProcessorUtils.getAllMethods(processingEnvironment, entity);
        List<JoinInfo> joins = new ArrayList<>();
        for (Element element : elements) {
            if (hasJoinAnnotation(element)) {
                ExecutableElement getter = ProcessorUtils.findGetter(processingEnvironment,
                        entity, element.getSimpleName());
                ExecutableElement setter = ProcessorUtils.findSetter(processingEnvironment,
                        entity, element.getSimpleName());
                joins.add(new JoinInfo(element, getter, setter));
            }
        }

        List<CodeBlock> blocks = new ArrayList<>();
        List<MethodSpec> specs = new ArrayList<>();
        for (ExecutableElement m : methods) {
            Optional<JoinInfo> join = joins.stream()
                    .filter(p -> isSameMethod(m, p.getter) || isSameMethod(m, p.setter))
                    .findFirst();
            if (join.isPresent()) {
                if (join.get().getter.equals(m) || m instanceof MockGetter) {
                    Map.Entry<CodeBlock, MethodSpec> generated = buildJoinMethod(entity, join.get());
                    specs.add(generated.getValue());
                    blocks.add(generated.getKey());
                } else if (ProcessorUtils.isCollectionType(join.get().field.asType())) {
                    specs.add(ProcessorUtils.buildDelegateMethod(processingEnvironment, m, entity, true, true, join.get().getter));
                } else {
                    specs.add(ProcessorUtils.buildDelegateMethod(processingEnvironment, m, entity, true, false, join.get().getter));
                }
            } else {
                specs.add(ProcessorUtils.buildDelegateMethod(processingEnvironment, m, entity, true, false, null));
            }
        }
        return new DelegationInfo(specs, blocks);
    }

    private boolean isSameMethod(ExecutableElement m, ExecutableElement getter) {
        Name name1 = m.getSimpleName();
        Name name2 = getter.getSimpleName();
        return name1.contentEquals(name2);
    }

    private boolean hasJoinAnnotation(Element ele) {
        return ele.getAnnotation(Relationship.class) != null;
    }

    private Map.Entry<CodeBlock, MethodSpec> buildJoinMethod(TypeElement entity, JoinInfo join) {
        ExecutableElement method = join.getter;
        ReturnTypeDefinition definition = new ReturnTypeDefinition(processingEnvironment, method.getReturnType());
        String runnerMethod;
        if (definition.isCollection()) {
            runnerMethod = "readAll";
        } else if (definition.isOptional()) {
            runnerMethod = "readOpt";
        } else if (definition.isCursor()) {
            runnerMethod = "readCursor";
        } else {
            runnerMethod = "read";
        }

        Relationship relationship = join.field.getAnnotation(Relationship.class);
        Relationship.RelationshipColumn[] columns = relationship.columns();

        CodeBlock.Builder builder = CodeBlock.builder()
                .addStatement(REQUIRE_NON_NULL, Objects.class)
                .beginControlFlow("if (this.entity.$L() == null)", join.getter.getSimpleName())
                .addStatement("$T params = new $T<>()", ParameterizedTypeName.get(List.class, SqlParameter.class), ArrayList.class);

        List<Relationship.RelationshipColumn> sorted = Arrays.stream(columns)
                .sorted(defaultComparator().reversed()) // Reversed so empty string (default value) are last
                .collect(Collectors.toList());
        for (Relationship.RelationshipColumn column : sorted) {
            VariableElement targetColumn = ProcessorUtils.getFieldWithColumnName(processingEnvironment,
                    definition.getRealClass(), column.targetColumn());
            buildJoinParam(column, builder, targetColumn, entity);
        }
        String wheres = createJoinWhere(sorted);
        CodeBlock block;
        if (ProcessorUtils.isCollectionType(join.field.asType())) {
            block = builder.addStatement("this.entity.$L(new $T<$T>(this, $T.getInstance($T.class).$L($T.class, $T.getInstance().getSimpleSql($T.class) +  $S, params)))",
                            ProcessorUtils.findSetter(processingEnvironment, entity, join.field.getSimpleName()).getSimpleName(),
                            TrackedList.class, definition.getRealClass(),
                            QueryRunner.class, definition.getRealClass(), runnerMethod, definition.getRealClass(), DelegatesService.class,
                            definition.getRealClass(), wheres)
                    .endControlFlow()
                    .addStatement("return this.entity.$L()", join.getter.getSimpleName())
                    .build();
        } else {
            block = builder.addStatement("this.entity.$L($T.getInstance($T.class).$L($T.class, $T.getInstance().getSimpleSql($T.class) +  $S, params))",
                            ProcessorUtils.findSetter(processingEnvironment, entity, join.field.getSimpleName()).getSimpleName(),
                            QueryRunner.class, definition.getRealClass(), runnerMethod, definition.getRealClass(), DelegatesService.class,
                            definition.getRealClass(), wheres)
                    .endControlFlow()
                    .addStatement("return this.entity.$L()", join.getter.getSimpleName())
                    .build();
        }

        CodeBlock relationshipInfo = generateRelInfo(join.field.getSimpleName(), wheres, entity, columns);
        MethodSpec delegate = MethodSpec.overriding(method)
                .addAnnotations(getAllAnnotations(method)) // Add all annotations that are available on getter
                .addCode(block)
                .build();

        return new AbstractMap.SimpleImmutableEntry<>(relationshipInfo, delegate);
    }

    private Iterable<AnnotationSpec> getAllAnnotations(ExecutableElement method) {
        return method.getAnnotationMirrors()
                .stream()
                .map(AnnotationSpec::get)
                .collect(Collectors.toList());
    }

    private Comparator<Relationship.RelationshipColumn> defaultComparator() {
        return (c1, c2) -> {
            if (!c1.sourceColumn().isEmpty()) {
                return -1;
            } else if (!c2.sourceColumn().isEmpty()) {
                return 1;
            }

            return 0;
        };
    }

    private CodeBlock generateRelInfo(Name simpleName, String wheres,
                                      TypeElement entity,
                                      Relationship.RelationshipColumn[] columns) {
        CodeBlock.Builder block = CodeBlock.builder()
                .add("$T.<$L>builder().where($S)", RelationshipManager.RelationshipInfo.Builder.class, entity, wheres);
        for (Relationship.RelationshipColumn column : columns) {
            if (!column.sourceColumn().isEmpty()) {
                block.add(".param(($T<$T, Object>) Column.findColumn($S).getGetter())", Function.class, entity, column.sourceColumn());
            } else {
                block.add(".param((e) -> $T.$L.toValue($S))", ParameterConverter.class, column.converter(), column.defaultValue());
            }
        }
        return CodeBlock.builder()
                .addStatement("manager.addRelationshipInfo($S, $L)",
                        simpleName, block.add(".build()").build())
                .build();
    }

    private String createJoinWhere(List<Relationship.RelationshipColumn> columns) {
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        for (Relationship.RelationshipColumn column : columns) {
            String target = column.targetColumn();
            if (first) {
                builder.append("WHERE ").append(target).append(WILDCARD);
                first = false;
            } else {
                builder.append("AND ").append(target).append(WILDCARD);
            }
        }

        return " " + builder;
    }

    private void buildJoinParam(Relationship.RelationshipColumn column, CodeBlock.Builder builder,
                                VariableElement targetColumn,
                                TypeElement entity) {
        if (!column.sourceColumn().isEmpty()) {
            VariableElement referenced = getReferencedColumn(column.sourceColumn(), entity);
            TypeMirror target = targetColumn.asType();
            if (referenced.getAnnotation(Converter.class) != null) {
                List<TypeElement> converterTypes = ProcessorUtils.getConverterTypes(processingEnvironment, referenced);
                target = converterTypes.get(0).asType();
            }
            builder.addStatement("params.add(new $T($LDelegate.Column.findColumn($S).getGetter().apply(this.entity), $T.find($T.class).getSetter()))",
                    SqlParameter.class, entity.getSimpleName(), column.sourceColumn(), SqlAccessor.class, target);
        } else {
            String defaultValue = column.defaultValue();
            builder.addStatement("params.add(new $T($T.$L.toValue($S), $T.find($T.$L.getKlass()).getSetter()))",
                    SqlParameter.class, ParameterConverter.class, column.converter(), defaultValue, SqlAccessor.class, ParameterConverter.class, column.converter());
        }
    }

    private VariableElement getReferencedColumn(String sourceColumn, TypeElement entity) {
        return entity.getEnclosedElements()
                .stream()
                .filter(e -> e.getAnnotation(Column.class) != null)
                .filter(e -> e.getAnnotation(Column.class).name().equalsIgnoreCase(sourceColumn))
                .findFirst()
                .map(VariableElement.class::cast)
                .orElseThrow(() -> new ProcessorException("Can't find referenced column"));
    }

    private FieldSpec addUpdateSql() {
        return FieldSpec.builder(String.class, "UPDATE_SQL", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .build();
    }

    private FieldSpec addInsertSql() {
        return FieldSpec.builder(String.class, "INSERT_SQL", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .build();
    }

    private FieldSpec addKeysWhere() {
        return FieldSpec.builder(String.class, KEYS_WHERE, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
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
                .addStatement("return ENTITY_MAPPER")
                .build();
        MethodSpec setEntity = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "setEntity", EntityDelegate.class))
                .addStatement("this.modified = false")
                .addStatement("this.entity = toEntity($L)", ProcessorUtils.extractParameterNames(ProcessorUtils.getMethod(processingEnvironment, "setEntity", EntityDelegate.class)))
                .build();
        MethodSpec setEntityObj = MethodSpec.methodBuilder("setFullEntity")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(entity), ENTITY)
                .addStatement("this.entity = entity")
                .build();
        MethodSpec getEntity = MethodSpec.methodBuilder("getEntity")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(entity))
                .addStatement("return this.entity")
                .build();
        MethodSpec baseSql = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getBaseSql", EntityDelegate.class))
                .addStatement("return \"SELECT \" + SELECTABLES + \" FROM \" + TABLE")
                .build();
        MethodSpec keysWhere = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getKeysWhere", EntityDelegate.class))
                .addStatement("return KEYS_WHERE")
                .build();
        MethodSpec insertSql = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getInsertSql", EntityDelegate.class))
                .addStatement("return INSERT_SQL")
                .build();
        MethodSpec selectables = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getSelectables", EntityDelegate.class))
                .addStatement("return SELECTABLES.replace($S,$S).replace($S,$S).split($S)", "(", "", ")", "", ",")
                .build();
        MethodSpec table = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getTable", EntityDelegate.class))
                .addStatement("return TABLE")
                .build();
        MethodSpec updateSql = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getUpdateSql", EntityDelegate.class))
                .addStatement("return UPDATE_SQL")
                .build();
        MethodSpec deleteSql = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getDeleteSql", EntityDelegate.class))
                .addStatement("return \"DELETE FROM \" + TABLE + KEYS_WHERE")
                .build();
        MethodSpec modified = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "isModified", EntityDelegate.class))
                .addStatement("return this.modified")
                .build();
        MethodSpec setModified = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "setModified", EntityDelegate.class))
                .addStatement("this.modified = arg0")
                .build();
        MethodSpec isDefaultGeneration = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "isDefaultGeneration", EntityDelegate.class))
                .addStatement("return $L", hasDefaultGenerated(entity))
                .build();
        MethodSpec setFullEntityFullColumns = MethodSpec.methodBuilder("setFullEntityFullColumns")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(
                        ParameterizedTypeName.get(
                                ClassName.get(Map.class),
                                ParameterizedTypeName.get(ClassName.get(SqlColumn.class), TypeName.get(entity.asType()), WildcardTypeName.subtypeOf(Object.class)),
                                WildcardTypeName.subtypeOf(Object.class)
                        ),
                        "map"
                )
                .addCode(buildFullEntityColumnsCode(entity))
                .build();
        MethodSpec getKeyWhere = MethodSpec.methodBuilder("getKeysWhere")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(String.class), "alias")
                .returns(String.class)
                .addCode(buildGetKeys())
                .build();

        MethodSpec initDefault;
        if (hasDefaultGenerated(entity)) {
            initDefault = buildInitDefault(entity);
        } else {
            initDefault = getInitDefault(entity)
                    .addStatement("return e")
                    .build();
        }

        MethodSpec toTableInfo = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "toTableInfo", EntityDelegate.class))
                .addStatement("return new $T(TABLE, this.entityClass, SCHEMA)", TableInfo.class)
                .build();

        MethodSpec generateDelegate = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "generateDelegate", EntityDelegate.class))
                .returns(ParameterizedTypeName.get(ClassName.get(EntityDelegate.class), ClassName.get(entity)))
                .addStatement("return new $LDelegate()", entity)
                .build();

        MethodSpec getTracker = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getTracker", EntityDelegate.class))
                .returns(ParameterizedTypeName.get(ClassName.get(DirtinessTracker.class), ClassName.get(entity)))
                .addStatement("return this.tracker")
                .build();

        MethodSpec isLazyEntity = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "isLazyEntity", EntityDelegate.class))
                .addStatement("return this.lazyInfo != null")
                .build();

        MethodSpec getLazyInfo = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getLazyInfo", EntityDelegate.class))
                .addStatement("return this.lazyInfo")
                .build();

        MethodSpec setLazyInfo = MethodSpec.methodBuilder("setLazyInfo")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(LazyEntityInfo.class, "info")
                .addStatement("this.lazyInfo = info")
                .build();

        MethodSpec getRelationshipManager = MethodSpec.methodBuilder("getRelationshipManager")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(ClassName.get(RelationshipManager.class), ClassName.get(entity)))
                .addStatement("return RELATIONSHIP_MANAGER")
                .build();

        return Stream.of(supplierEntity, entityMapper,
                setEntity, setEntityObj, baseSql, keysWhere,
                insertSql, selectables, table, updateSql,
                getEntity, deleteSql, modified,
                isDefaultGeneration, initDefault, generateDelegate,
                setFullEntityFullColumns, getKeyWhere, toTableInfo, getTracker,
                getLazyInfo, isLazyEntity, setLazyInfo, getRelationshipManager,
                setModified)
                .collect(Collectors.toList());
    }

    private MethodSpec.Builder getInitDefault(TypeElement entity) {
        MethodSpec.Builder builder = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "initDefault", EntityDelegate.class))
                .returns(TypeName.get(entity.asType()));
        builder.parameters.set(0, ParameterSpec.builder(TypeName.get(entity.asType()), "e").build());
        return builder;
    }

    private MethodSpec buildInitDefault(TypeElement entity) {
        MethodSpec.Builder builder = getInitDefault(entity);
        entity.getEnclosedElements()
                .stream()
                .filter(e -> DEFAULTS.stream().anyMatch(c -> e.getAnnotation(c) != null))
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .forEach(e -> {
                    DefaultTemporal temporal = e.getAnnotation(DefaultTemporal.class);
                    DefaultString string = e.getAnnotation(DefaultString.class);
                    DefaultNumeric numeric = e.getAnnotation(DefaultNumeric.class);

                    if (temporal != null) {
                        if (!DefaultTemporal.DEFAULT_FORMAT.equalsIgnoreCase(temporal.format())) {
                            builder.addStatement(
                                    "e.$L($T.forTemporal($T.class, $S, $S))",
                                    ProcessorUtils.findSetter(processingEnvironment, entity, e.getSimpleName())
                                            .getSimpleName(),
                                    DefaultGenerator.class,
                                    e.asType(),
                                    temporal.format(),
                                    temporal.value()
                            );
                        } else {
                            builder.addStatement(
                                    "e.$L($T.forTemporal($T.class))",
                                    ProcessorUtils.findSetter(processingEnvironment, entity, e.getSimpleName())
                                            .getSimpleName(),
                                    DefaultGenerator.class,
                                    e.asType()
                            );
                        }
                    }

                    if (string != null) {
                        builder.addStatement(
                                "e.$L($S)",
                                ProcessorUtils.findSetter(processingEnvironment, entity, e.getSimpleName())
                                        .getSimpleName(),
                                string.value()
                        );
                    }

                    if (numeric != null) {
                        builder.addStatement(
                                "e.$L($T.forNumeric($T.class, $L))",
                                ProcessorUtils.findSetter(processingEnvironment, entity, e.getSimpleName())
                                        .getSimpleName(),
                                DefaultGenerator.class,
                                e.asType(),
                                numeric.value()
                        );
                    }
                });
        return builder.addStatement("return e")
                .build();
    }

    private boolean hasDefaultGenerated(TypeElement entity) {
        return entity.getEnclosedElements()
                .stream()
                .anyMatch(e -> DEFAULTS.stream().anyMatch(c -> e.getAnnotation(c) != null));
    }

    private CodeBlock buildGetKeys() {
        return CodeBlock.builder()
                .addStatement("$L keys = $T.of(Column.values()).filter(col -> col.key).map(col -> col.colName).collect($T.toList())",
                        ParameterizedTypeName.get(List.class, String.class), Stream.class, Collectors.class)
                .addStatement(SET_FIRST)
                .addStatement(BUILDER_INSTANCE, StringBuilder.class, StringBuilder.class)
                .beginControlFlow("for (String key : keys)")
                .beginControlFlow(CHECK_FIRST)
                .addStatement("builder.append($S).append(String.format(\"%s.%s\", alias, key)).append($S)", WHERE, WILDCARD)
                .addStatement(RESET_FIRST)
                .nextControlFlow("else")
                .addStatement("builder.append($S).append(String.format(\"%s.%s\", alias, key)).append($S)", AND, WILDCARD)
                .endControlFlow()
                .endControlFlow()
                .addStatement("return builder.toString()")
                .build();
    }

    private CodeBlock buildFullEntityColumnsCode(TypeElement entity) {
        return CodeBlock.builder()
                .addStatement("this.entity = this.getEntityInstance().get()")
                .beginControlFlow("for ($T entry : map.entrySet())",
                        ParameterizedTypeName.get(ClassName.get(Map.Entry.class),
                                ParameterizedTypeName.get(ClassName.get(SqlColumn.class), TypeName.get(entity.asType()), WildcardTypeName.subtypeOf(Object.class)),
                                WildcardTypeName.subtypeOf(Object.class)))
                .addStatement("Column col = Column.findColumn(entry.getKey().getName())")
                .addStatement("col.setter.accept(this, entry.getValue())")
                .endControlFlow()
                .build();
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
        builder.addMethod(addColumnConstructor(entity));
        for (Accessor accessor : accessors) {
            builder.addEnumConstant(accessor.name, enumImpl(accessor, entity));
        }

        addColumnsMethods(builder, entity);
        return builder.build();
    }

    private CodeBlock buildUpdateSqlGenerator(TypeElement entity) {
        String table = entity.getAnnotation(Table.class).name();
        return CodeBlock.builder()
                .addStatement(BUILDER_INSTANCE, StringBuilder.class, StringBuilder.class)
                .addStatement(BUILDER_SIMPLE_APPEND, "UPDATE " + table + " ")
                .addStatement(SET_FIRST)
                .beginControlFlow("for (int i = 0; i < Column.values().length; i++)")
                .addStatement("Column column = Column.values()[i]")
                .beginControlFlow(CHECK_FIRST)
                .addStatement(BUILDER_SIMPLE_APPEND, "SET ")
                .addStatement(RESET_FIRST)
                .endControlFlow()
                .addStatement("builder.append(column.colName).append($S)", " = ?")
                .beginControlFlow("if (i != Column.values().length - 1)")
                .addStatement(BUILDER_SIMPLE_APPEND, ", ")
                .endControlFlow()
                .endControlFlow()
                .addStatement("UPDATE_SQL = builder.toString()")
                .build();
    }

    private CodeBlock buildInsertSqlGenerator(TypeElement entity) {
        String table = entity.getAnnotation(Table.class).name();
        return CodeBlock.builder()
                .addStatement("$T genInstance = $T.getInstance()", GeneratorsService.class, GeneratorsService.class)
                .addStatement(BUILDER_INSTANCE, StringBuilder.class, StringBuilder.class)
                .addStatement(BUILDER_SIMPLE_APPEND, "INSERT INTO " + table + " (")
                .beginControlFlow("for (int i = 0; i < Column.values().length; i++)")
                .addStatement("Column column = Column.values()[i]")
                .beginControlFlow("if (column.autoGenerated && !genInstance.canGenerateValue($T.class, column.colName))", entity)
                .addStatement("continue")
                .endControlFlow()
                .addStatement("builder.append(column.colName)")
                .beginControlFlow("if (i != Column.values().length - 1)")
                .addStatement(BUILDER_SIMPLE_APPEND, ",")
                .endControlFlow()
                .endControlFlow()
                .addStatement("String wildcards = Stream.of(Column.values()).filter(c -> !c.autoGenerated || genInstance.canGenerateValue($T.class, c.colName)).map(c -> $S).collect($T.joining($S))", entity, "?", Collectors.class, ",")
                .addStatement("builder.append($S).append(wildcards).append($S)", ") VALUES (", ")")
                .addStatement("INSERT_SQL = builder.toString()")
                .build();
    }

    private CodeBlock buildKeysWhereGenerator() {
        return CodeBlock.builder()
                .addStatement("$L keys = $T.of(Column.values()).filter(col -> col.key).map(col -> col.colName).collect($T.toList())",
                        ParameterizedTypeName.get(List.class, String.class), Stream.class, Collectors.class)
                .addStatement(SET_FIRST)
                .addStatement(BUILDER_INSTANCE, StringBuilder.class, StringBuilder.class)
                .beginControlFlow("for (String key : keys)")
                .beginControlFlow(CHECK_FIRST)
                .addStatement("builder.append($S).append(key).append($S)", WHERE, WILDCARD)
                .addStatement(RESET_FIRST)
                .nextControlFlow("else")
                .addStatement("builder.append($S).append(key).append($S)", AND, WILDCARD)
                .endControlFlow()
                .endControlFlow()
                .addStatement("KEYS_WHERE = builder.toString()")
                .build();
    }

    private CodeBlock buildSelectablesGenerator() {
        return CodeBlock.builder()
                .addStatement("SELECTABLES = $T.of(Column.values()).map(col -> col.colName).collect($T.joining($S))",
                        Stream.class, Collectors.class, ",")
                .build();
    }

    private CodeBlock buildEntityMapperGenerator(TypeElement entity) {
        return CodeBlock.builder()
                .addStatement("$T.Builder<$T> builder = new $T.Builder<>()", EntityMapper.class, entity, EntityMapper.class)
                .beginControlFlow("for (Column col : Column.values())")
                .addStatement("builder.add(col.colName, col.type, col.setter, col.getter, col.key, col.autoGenerated)")
                .endControlFlow()
                .addStatement("ENTITY_MAPPER = builder.build()")
                .build();
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
                MethodSpec.methodBuilder("findColumn")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.bestGuess("Column"))
                        .addParameter(String.class, COL_NAME)
                        .addModifiers(Modifier.STATIC)
                        .addCode(buildFindColumnCodeBlock())
                        .build()
        );
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

    private static class JoinInfo {

        private final Element field;
        private final ExecutableElement getter;
        private final ExecutableElement setter;

        private JoinInfo(Element field, ExecutableElement getter, ExecutableElement setter) {
            this.field = field;
            this.getter = getter;
            this.setter = setter;
        }
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
            boolean autoGenerated = (key && element.getAnnotation(Id.class).autoGenerated())
                    || Optional.ofNullable(element.getAnnotation(Column.class)).map(Column::autoGenerated).orElse(false);
            Converter converter = element.getAnnotation(Converter.class);
            String converterInstance = null;
            TypeElement beforeConverterClass = null;
            ExecutableElement getter = ProcessorUtils.findGetter(processingEnvironment, entity, element.getSimpleName());
            ExecutableElement setter = ProcessorUtils.findSetter(processingEnvironment, entity, element.getSimpleName());
            if (converter != null) {
                converterInstance = ProcessorUtils.getConverterCaller(processingEnvironment, element);
                beforeConverterClass = ProcessorUtils.getConverterTypes(processingEnvironment, element)
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
