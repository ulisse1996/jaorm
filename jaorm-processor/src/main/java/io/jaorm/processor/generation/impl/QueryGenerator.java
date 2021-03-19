package io.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.jaorm.Arguments;
import io.jaorm.BaseDao;
import io.jaorm.DaoImplementation;
import io.jaorm.annotation.Dao;
import io.jaorm.annotation.Query;
import io.jaorm.annotation.Table;
import io.jaorm.cache.Cacheable;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.logger.JaormLogger;
import io.jaorm.processor.generation.Generator;
import io.jaorm.processor.strategy.QueryStrategy;
import io.jaorm.processor.util.GeneratedFile;
import io.jaorm.processor.util.ProcessorUtils;
import io.jaorm.processor.util.ReturnTypeDefinition;
import io.jaorm.spi.DelegatesService;
import io.jaorm.spi.QueriesService;
import io.jaorm.spi.QueryRunner;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryGenerator extends Generator {

    private static final String MAP_FORMAT = "$T values = new $T<>()";
    private static final String REQUIRED_NOT_NULL_STATEMENT = "$T.requireNonNull(arg0, $S)";
    private static final String ENTITY_CAN_T_BE_NULL = "Entity can't be null !";
    private static final JaormLogger logger = JaormLogger.getLogger(QueryGenerator.class);

    public QueryGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<TypeElement> entities = roundEnvironment.getElementsAnnotatedWith(Table.class)
                .stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toList());
        List<TypeElement> queries = ProcessorUtils.getAllDao(roundEnvironment);
        for (TypeElement query : queries) {
            logger.debug(() -> "Generating Query Implementation for Query " + query);
            Set<MethodSpec> methods = new HashSet<>();
            for (Element ele : processingEnvironment.getElementUtils().getAllMembers(query)) {
                if (ele.getAnnotation(Query.class) != null) {
                    ExecutableElement executableElement = (ExecutableElement) ele;
                    MethodSpec methodSpec = buildImpl(entities, executableElement);
                    methods.add(methodSpec);
                }
            }

            List<AnnotationSpec> annotations = getExtraAnnotations(query);

            if (ProcessorUtils.isBaseDao(query)) {
                methods.addAll(buildBaseDao(query));
            }

            String packageName = getPackage(query);
            TypeSpec build = TypeSpec.classBuilder(query.getSimpleName() + "Impl")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotations(annotations)
                    .addSuperinterface(query.asType())
                    .addMethods(methods)
                    .build();
            ProcessorUtils.generate(processingEnvironment,
                    new GeneratedFile(packageName, build, query.getQualifiedName().toString()));
        }

        if (!queries.isEmpty()) {
            buildQueries(queries);
        }
    }

    private void buildQueries(List<TypeElement> types) {
        TypeSpec queries = TypeSpec.classBuilder("Queries")
                .addModifiers(Modifier.PUBLIC)
                .superclass(QueriesService.class)
                .addField(queriesMap(), "queries", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(queriesConstructor(types))
                .addMethod(buildGetQueries())
                .build();
        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, queries, ""));
    }

    private MethodSpec buildGetQueries() {
        return MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getQueries", QueriesService.class))
                .addStatement("return this.queries")
                .build();
    }

    private MethodSpec queriesConstructor(List<TypeElement> types) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement(MAP_FORMAT, queriesMap(), HashMap.class);
        types.forEach(type -> {
            TypeElement baseType;
            if (ProcessorUtils.isBaseDao(type)) {
                baseType = processingEnvironment.getElementUtils().getTypeElement(ProcessorUtils.getBaseDaoGeneric(type));
            } else {
                baseType = processingEnvironment.getElementUtils().getTypeElement(Object.class.getName());
            }
            builder.addStatement("values.put($L.class, new $T($T.class, $LImpl::new))",
                    type.getQualifiedName(), DaoImplementation.class, baseType, type.getQualifiedName());
        });
        builder.addStatement("this.queries = values");
        return builder.build();
    }

    private TypeName queriesMap() {
        return ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),
                ClassName.get(DaoImplementation.class)
        );
    }

    private String getPackage(TypeElement query) {
        return ClassName.get(query).packageName();
    }

    private Collection<MethodSpec> buildBaseDao(TypeElement query) {
        String realClass = ProcessorUtils.getBaseDaoGeneric(query);
        ClassName className = ClassName.bestGuess(realClass);
        MethodSpec read = resolveParameter(MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "read", BaseDao.class))
                .returns(className)
                .addStatement(REQUIRED_NOT_NULL_STATEMENT, Objects.class, ENTITY_CAN_T_BE_NULL)
                .addStatement("$T arguments = $T.getInstance().asWhere($L)", Arguments.class, DelegatesService.class, "arg0")
                .addStatement("return $T.getCached($T.class, arguments, () -> $T.getInstance($T.class).read($T.class, $T.getInstance().getSql($T.class), argumentsAsParameters($T.getInstance().asWhere($L).getValues())))",
                        Cacheable.class, className, QueryRunner.class, className, className, DelegatesService.class, className, DelegatesService.class, "arg0"), realClass);
        MethodSpec readOpt = resolveParameter(MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "readOpt", BaseDao.class))
                .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), className))
                .addStatement(REQUIRED_NOT_NULL_STATEMENT, Objects.class, ENTITY_CAN_T_BE_NULL)
                .addStatement("$T arguments = $T.getInstance().asWhere($L)", Arguments.class, DelegatesService.class, "arg0")
                .addStatement("return $T.getCachedOpt($T.class, arguments, () -> $T.getInstance($T.class).readOpt($T.class, $T.getInstance().getSql($T.class), argumentsAsParameters($T.getInstance().asWhere($L).getValues())))",
                        Cacheable.class, className, QueryRunner.class, className, className, DelegatesService.class, className, DelegatesService.class, "arg0"), realClass);
        MethodSpec readAll = resolveParameter(MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "readAll", BaseDao.class))
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), className))
                .addStatement("return $T.getCachedAll($T.class, () -> $T.getInstance($T.class).readAll($T.class, $T.getInstance().getSimpleSql($T.class), $T.emptyList()))",
                        Cacheable.class, className, QueryRunner.class, className, className, DelegatesService.class, className, Collections.class), realClass);
        return Stream.of(read, readOpt, readAll).collect(Collectors.toList());
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
        String sql = query.sql();
        MethodSpec.Builder builder = MethodSpec.overriding(executableElement)
                .addStatement("$T params = new $T<>()",
                        ParameterizedTypeName.get(List.class, SqlParameter.class), ArrayList.class);
        for (QueryStrategy queryStrategy : QueryStrategy.values()) {
            if (queryStrategy.isValid(sql)) {
                List<CodeBlock> statements = queryStrategy.extract(this.processingEnvironment, sql, executableElement);
                statements.forEach(builder::addCode);
                sql = queryStrategy.replaceQuery(sql);
                Map.Entry<String, Object[]> checked = checkType(entities, sql, executableElement);
                builder.addStatement(checked.getKey(), checked.getValue());
                break;
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
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getInstance($T.class).readOpt($T.class, $S, params)",
                    stmParams
            );
        } else if (definition.isCollection()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getInstance($T.class).readAll($T.class, $S, params)",
                    stmParams
            );
        } else if (definition.isStream()) {
            if (!definition.isStreamTableRow()) {
                return new AbstractMap.SimpleImmutableEntry<>(
                        "return $T.getSimple().readStream($T.class, $S, params)",
                        new Object[] {QueryRunner.class, definition.getRealClass(), sql}
                );
            } else {
                return new AbstractMap.SimpleImmutableEntry<>(
                        "return $T.getSimple().readStream($S, params)",
                        new Object[] {QueryRunner.class, sql}
                );
            }
        } else if (definition.isTableRow()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    "return $T.getSimple().read($S, params)",
                    new Object[] {QueryRunner.class, sql}
            );
        } else if (entities.contains(definition.getRealClass())) {
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
}
