package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.Sort;
import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.cache.Cacheable;
import io.github.ulisse1996.jaorm.entity.Page;
import io.github.ulisse1996.jaorm.entity.PageImpl;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.strategy.QueryStrategy;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.util.ReturnTypeDefinition;
import io.github.ulisse1996.jaorm.specialization.DoubleKeyDao;
import io.github.ulisse1996.jaorm.specialization.SingleKeyDao;
import io.github.ulisse1996.jaorm.specialization.TripleKeyDao;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.provider.QueryProvider;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryGenerator extends Generator {

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM %s";
    private static final String REQUIRED_NOT_NULL_STATEMENT = "$T.requireNonNull(arg0, $S)";
    private static final String ENTITY_CAN_T_BE_NULL = "Entity can't be null !";
    protected static final String CREATE_ENTITY = "$T entity = new $T()";
    protected static final String ENTITY_SETTER = "entity.$L($L)";

    public QueryGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<GeneratedFile> files = new ArrayList<>();
        List<TypeElement> entities = roundEnvironment.getElementsAnnotatedWith(Table.class)
                .stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toList());
        List<TypeElement> queries = ProcessorUtils.getAllDao(roundEnvironment);
        for (TypeElement query : queries) {
            debugMessage("Generating Query Implementation for Query " + query);
            Set<MethodSpec> methods = new HashSet<>();
            for (Element ele : processingEnvironment.getElementUtils().getAllMembers(query)) {
                if (ele.getAnnotation(Query.class) != null) {
                    ExecutableElement executableElement = (ExecutableElement) ele;
                    MethodSpec methodSpec = buildImpl(entities, executableElement);
                    methods.add(methodSpec);
                }
            }

            List<AnnotationSpec> annotations = getExtraAnnotations(query);

            if (ProcessorUtils.isBaseDao(processingEnvironment, query)) {
                methods.addAll(buildBaseDao(query));
            }

            String packageName = getPackage(query);
            TypeSpec build = TypeSpec.classBuilder(query.getSimpleName() + "Impl")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotations(annotations)
                    .addSuperinterface(query.asType())
                    .addSuperinterface(QueryProvider.class)
                    .addMethods(methods)
                    .addMethods(getProviderMethods(query.getSimpleName() + "Impl", query))
                    .build();
            GeneratedFile file = new GeneratedFile(packageName, build, query.getQualifiedName().toString());
            ProcessorUtils.generate(processingEnvironment, file);
            files.add(file);
        }

        ProcessorUtils.generateSpi(processingEnvironment, files, QueryProvider.class);
    }

    private Iterable<MethodSpec> getProviderMethods(String queryName, TypeElement type) {
        TypeElement baseType;
        if (ProcessorUtils.isBaseDao(processingEnvironment, type)) {
            baseType = processingEnvironment.getElementUtils().getTypeElement(ProcessorUtils.getBaseDaoGeneric(processingEnvironment, type));
        } else {
            baseType = processingEnvironment.getElementUtils().getTypeElement(Object.class.getName());
        }

        MethodSpec getEntityClass = MethodSpec.overriding(ProcessorUtils.getMethod(this.processingEnvironment, "getEntityClass", QueryProvider.class))
                .addStatement("return $T.class", baseType)
                .build();

        MethodSpec getQuerySupplier = MethodSpec.overriding(ProcessorUtils.getMethod(this.processingEnvironment, "getQuerySupplier", QueryProvider.class))
                .addStatement("return $L::new", queryName)
                .build();

        MethodSpec getDaoClass = MethodSpec.overriding(ProcessorUtils.getMethod(this.processingEnvironment, "getDaoClass", QueryProvider.class))
                .addStatement("return $T.class", type)
                .build();

        return Arrays.asList(getEntityClass, getQuerySupplier, getDaoClass);
    }

    private String getPackage(TypeElement query) {
        return ClassName.get(query).packageName();
    }

    private Collection<MethodSpec> buildBaseDao(TypeElement query) {
        String realClass = ProcessorUtils.getBaseDaoGeneric(processingEnvironment, query);
        TypeElement element = processingEnvironment.getElementUtils().getTypeElement(realClass);
        boolean singleKey = checkSubTypeDao(query, SingleKeyDao.class);
        boolean doubleKey = checkSubTypeDao(query, DoubleKeyDao.class);
        boolean tripleKey = checkSubTypeDao(query, TripleKeyDao.class);
        Table table = element.getAnnotation(Table.class);
        ClassName className = ClassName.bestGuess(realClass);
        MethodSpec read = resolveParameter(MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "read", BaseDao.class))
                .returns(className)
                .addStatement(REQUIRED_NOT_NULL_STATEMENT, Objects.class, ENTITY_CAN_T_BE_NULL)
                .addStatement("$T arguments = $T.getInstance().asWhere($L)", Arguments.class, DelegatesService.class, "arg0")
                .addStatement("return $T.getCached($T.class, arguments, () -> $T.getInstance($T.class).read($T.class, $T.getInstance().getSql($T.class), argumentsAsParameters(arguments.getValues())))",
                        Cacheable.class, className, QueryRunner.class, className, className, DelegatesService.class, className), realClass);
        MethodSpec readOpt = resolveParameter(MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "readOpt", BaseDao.class))
                .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), className))
                .addStatement(REQUIRED_NOT_NULL_STATEMENT, Objects.class, ENTITY_CAN_T_BE_NULL)
                .addStatement("$T arguments = $T.getInstance().asWhere($L)", Arguments.class, DelegatesService.class, "arg0")
                .addStatement("return $T.getCachedOpt($T.class, arguments, () -> $T.getInstance($T.class).readOpt($T.class, $T.getInstance().getSql($T.class), argumentsAsParameters(arguments.getValues())).toOptional())",
                        Cacheable.class, className, QueryRunner.class, className, className, DelegatesService.class, className), realClass);
        MethodSpec readAll = resolveParameter(MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "readAll", BaseDao.class))
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), className))
                .addStatement("return $T.getCachedAll($T.class, () -> $T.getInstance($T.class).readAll($T.class, $T.getInstance().getSimpleSql($T.class), $T.emptyList()))",
                        Cacheable.class, className, QueryRunner.class, className, className, DelegatesService.class, className, Collections.class), realClass);
        MethodSpec.Builder page = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "page", BaseDao.class))
                .returns(ParameterizedTypeName.get(ClassName.get(Page.class), className))
                .addStatement("long count = $T.getSimple().read(Long.class, $S, $T.emptyList())", QueryRunner.class, String.format(COUNT_SQL, table.name()), Collections.class)
                .addStatement("return count > 0 ? new $T<$T>(arg0, arg1, count, $T.class, arg2) : $T.empty()", PageImpl.class, className, className, Page.class);
        page.parameters.set(2, getSortsType(className)); // Fix generics override

        List<MethodSpec> specializations = new ArrayList<>();
        if (singleKey) {
            specializations.addAll(buildSingleKeyDao(element));
        } else if (doubleKey || tripleKey) {
            specializations.addAll(buildMultipleKeyDao(element));
        }

        List<MethodSpec> gens = Stream.of(read, readOpt, readAll, page.build())
                .collect(Collectors.toList());
        gens.addAll(specializations);
        return gens;
    }

    private Collection<MethodSpec> buildMultipleKeyDao(TypeElement element) {
        List<VariableElement> ids =  ProcessorUtils.getAllValidElements(this.processingEnvironment, element)
                .stream()
                .filter(el -> el.getAnnotation(Id.class) != null)
                .map(VariableElement.class::cast)

                // We can use source order as stated in Element docs, but a custom sorting is
                // preferred for avoid conflicts caused by source refactoring when one or more parameters
                // have same Type
                .sorted(Comparator.comparing(v -> v.getSimpleName().toString()))
                .collect(Collectors.toList());

        MethodSpec.Builder read = MethodSpec.methodBuilder("readByKeys")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(element.asType()))
                .addStatement(CREATE_ENTITY, element, element);

        MethodSpec.Builder readOpt = MethodSpec.methodBuilder("readOptByKeys")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(element.asType())))
                .addStatement(CREATE_ENTITY, element, element);

        MethodSpec.Builder delete = MethodSpec.methodBuilder("deleteByKeys")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.INT)
                .addStatement(CREATE_ENTITY, element, element);

        for (VariableElement id : ids) {
            ExecutableElement setter = ProcessorUtils.findSetter(processingEnvironment, element, id.getSimpleName());
            TypeName paramType = TypeName.get(id.asType());
            if (paramType.isPrimitive()) {
                paramType = paramType.box(); // We need to match Generic Type for overriding
            }

            read.addParameter(paramType, id.getSimpleName().toString());
            read.addStatement(ENTITY_SETTER, setter.getSimpleName(), id.getSimpleName().toString());

            readOpt.addParameter(paramType, id.getSimpleName().toString());
            readOpt.addStatement(ENTITY_SETTER, setter.getSimpleName(), id.getSimpleName().toString());

            delete.addParameter(paramType, id.getSimpleName().toString());
            delete.addStatement(ENTITY_SETTER, setter.getSimpleName(), id.getSimpleName().toString());
        }

        read.addStatement("return read(entity)");
        readOpt.addStatement("return readOpt(entity)");
        delete.addStatement("return delete(entity)");

        return Arrays.asList(read.build(), readOpt.build(), delete.build());
    }

    private Collection<MethodSpec> buildSingleKeyDao(TypeElement element) {
        VariableElement id = ProcessorUtils.getAllValidElements(this.processingEnvironment, element)
                .stream()
                .filter(el -> el.getAnnotation(Id.class) != null)
                .map(VariableElement.class::cast)
                .findFirst()
                .orElseThrow(() -> new ProcessorException(String.format("Can't find Id from Entity %s", element)));
        ExecutableElement setter = ProcessorUtils.findSetter(processingEnvironment, element, id.getSimpleName());

        TypeName paramType = TypeName.get(id.asType());
        if (paramType.isPrimitive()) {
            paramType = paramType.box(); // We need to match Generic Type for overriding
        }

        MethodSpec read = MethodSpec.methodBuilder("readByKey")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(element.asType()))
                .addParameter(paramType, id.getSimpleName().toString())
                .addStatement(CREATE_ENTITY, element, element)
                .addStatement(ENTITY_SETTER, setter.getSimpleName(), id.getSimpleName().toString())
                .addStatement("return read(entity)")
                .build();

        MethodSpec readOpt = MethodSpec.methodBuilder("readOptByKey")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(element.asType())))
                .addParameter(paramType, id.getSimpleName().toString())
                .addStatement(CREATE_ENTITY, element, element)
                .addStatement(ENTITY_SETTER, setter.getSimpleName(), id.getSimpleName().toString())
                .addStatement("return readOpt(entity)")
                .build();

        MethodSpec delete = MethodSpec.methodBuilder("deleteByKey")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.INT)
                .addParameter(paramType, id.getSimpleName().toString())
                .addStatement(CREATE_ENTITY, element, element)
                .addStatement(ENTITY_SETTER, setter.getSimpleName(), id.getSimpleName().toString())
                .addStatement("return delete(entity)")
                .build();

        return Arrays.asList(read, readOpt, delete);
    }

    private boolean checkSubTypeDao(TypeElement query, Class<?> type) {
        return ProcessorUtils.isSubType(processingEnvironment, query, type);
    }

    private ParameterSpec getSortsType(ClassName className) {
        return ParameterSpec.builder(ParameterizedTypeName.get(
                ClassName.get(List.class),
                ParameterizedTypeName.get(ClassName.get(Sort.class), className)), "arg2")
                .build();
    }

    private List<AnnotationSpec> getExtraAnnotations(TypeElement query) {
        return query.getAnnotationMirrors()
                .stream()
                .map(AnnotationSpec::get)
                .filter(an -> !an.type.equals(TypeName.get(Dao.class)))
                .collect(Collectors.toList());
    }

    private MethodSpec resolveParameter(MethodSpec.Builder builder, String realClass) {
        List<ParameterSpec> specs = builder.parameters;
        for (int i = 0; i < specs.size(); i++) {
            ParameterSpec spec = specs.get(i);
            if (!spec.type.equals(ClassName.bestGuess(realClass))) {
                ParameterSpec newSpec = ParameterSpec.builder(ClassName.bestGuess(realClass), spec.name)
                        .build();
                specs.set(i, newSpec);
            }
        }

        return builder.build();
    }

    private MethodSpec buildImpl(List<TypeElement> entities, ExecutableElement executableElement) {
        Query query = executableElement.getAnnotation(Query.class);
        String sql = ProcessorUtils.getSqlOrSqlFromFile(query.sql(), this.processingEnvironment);
        MethodSpec.Builder builder = MethodSpec.overriding(executableElement)
                .addStatement("$T params = new $T<>()",
                        ParameterizedTypeName.get(List.class, SqlParameter.class), ArrayList.class);
        for (QueryStrategy queryStrategy : QueryStrategy.values()) {
            if (queryStrategy.isValid(sql, query.noArgs())) {
                List<CodeBlock> statements = queryStrategy.extract(this.processingEnvironment, sql, executableElement);
                statements.forEach(builder::addCode);
                sql = queryStrategy.replaceQuery(sql);
                Map.Entry<String, Object[]> checked = checkType(entities, sql, executableElement);
                builder.addStatement(checked.getKey(), checked.getValue());
                return builder.build();
            }
        }

        return builder.build();
    }

    private Map.Entry<String, Object[]> checkType(List<TypeElement> entities, String sql, ExecutableElement method) {
        if (sql.toUpperCase().startsWith("SELECT")) {
            return checkSelect(entities, sql, method);
        } else if (sql.toUpperCase().startsWith("DELETE")) {
            return new AbstractMap.SimpleImmutableEntry<>("$T.getSimple().delete($S, params)",
                    new Object[] {QueryRunner.class, sql});
        } else {
            return new AbstractMap.SimpleImmutableEntry<>("$T.getSimple().update($S, params)",
                    new Object[] {QueryRunner.class, sql});
        }
    }

    private Map.Entry<String, Object[]> checkSelect(List<TypeElement> entities, String sql, ExecutableElement method) {
        ReturnTypeDefinition definition = new ReturnTypeDefinition(processingEnvironment, method.getReturnType());
        Object[] stmParams = {
                QueryRunner.class, definition.getRealClass(), definition.getRealClass(), sql
        };
        if (definition.isOptional()) {
            return checkOptional(sql, definition, stmParams);
        } else if (definition.isCollection()) {
            return checkCollection(sql, definition, stmParams);
        } else if (definition.isStream()) {
            return checkStream(sql, definition);
        } else if (definition.isCursor()) {
            return checkCursor(sql, definition);
        } else if (definition.isTableRow()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getSimple().read($S, params)",
                    new Object[] {QueryRunner.class, sql}
            );
        } else if (entities.contains(definition.getRealClass()) || (definition.getRealClass() != null && definition.getRealClass().getAnnotation(Projection.class) != null)) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getInstance($T.class).read($T.class, $S, params)",
                    stmParams
            );
        } else {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getSimple().read($T.class, $S, params)",
                    new Object[] {QueryRunner.class, definition.getRealClass(), sql}
            );
        }
    }

    private Map.Entry<String, Object[]> checkCursor(String sql, ReturnTypeDefinition definition) {
        return new AbstractMap.SimpleImmutableEntry<>(
                "return $T.getInstance($T.class).readCursor($T.class, $S, params)",
                new Object[]{QueryRunner.class, definition.getRealClass(), definition.getRealClass(), sql}
        );
    }

    private static AbstractMap.SimpleImmutableEntry<String, Object[]> checkStream(String sql, ReturnTypeDefinition definition) {
        if (!definition.isStreamTableRow()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getInstance($T.class).readStream($T.class, $S, params)",
                    new Object[]{QueryRunner.class, definition.getRealClass(), definition.getRealClass(), sql}
            );
        } else {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getSimple().readStream($S, params)",
                    new Object[]{QueryRunner.class, sql}
            );
        }
    }

    private static AbstractMap.SimpleImmutableEntry<String, Object[]> checkCollection(String sql, ReturnTypeDefinition definition, Object[] stmParams) {
        if (definition.isTableRow()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getSimple().readStream($S, params).collect($T.toList())",
                    new Object[]{QueryRunner.class, sql, Collectors.class}
            );
        } else {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getInstance($T.class).readAll($T.class, $S, params)",
                    stmParams
            );
        }
    }

    private static AbstractMap.SimpleImmutableEntry<String, Object[]> checkOptional(String sql, ReturnTypeDefinition definition, Object[] stmParams) {
        if (definition.isTableRow()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getSimple().readOpt($S, params)",
                    new Object[]{QueryRunner.class, sql}
            );
        } else {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getInstance($T.class).readOpt($T.class, $S, params).toOptional()",
                    stmParams
            );
        }
    }
}
