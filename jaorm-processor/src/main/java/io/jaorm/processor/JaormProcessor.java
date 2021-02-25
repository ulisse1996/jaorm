package io.jaorm.processor;

import com.squareup.javapoet.*;
import io.jaorm.DaoImplementation;
import io.jaorm.cache.EntityCache;
import io.jaorm.logger.JaormLogger;
import io.jaorm.processor.annotation.CascadeType;
import io.jaorm.entity.relationship.EntityEventType;
import io.jaorm.entity.relationship.Relationship;
import io.jaorm.processor.util.RelationshipAccessor;
import io.jaorm.processor.util.RelationshipInfo;
import io.jaorm.spi.CacheService;
import io.jaorm.spi.DelegatesService;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.spi.QueriesService;
import io.jaorm.processor.annotation.Cacheable;
import io.jaorm.processor.annotation.Dao;
import io.jaorm.processor.annotation.Query;
import io.jaorm.processor.annotation.Table;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.util.MethodUtils;
import io.jaorm.spi.RelationshipService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedAnnotationTypes("io.jaorm.processor.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JaormProcessor extends AbstractProcessor {

    private static final JaormLogger logger = JaormLogger.getLogger(JaormProcessor.class);
    private static final String MAP_FORMAT = "$T values = new $T<>()";
    private static final String JAORM_PACKAGE = "io.jaorm.entity";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> queries = roundEnv.getElementsAnnotatedWith(Query.class);
        Set<? extends Element> dao = roundEnv.getElementsAnnotatedWith(Dao.class);
        Set<TypeElement> types = Stream.concat(queries.stream(), dao.stream())
                .map(ele -> {
                    if (ele instanceof ExecutableElement) {
                        return ele.getEnclosingElement();
                    } else {
                        return ele;
                    }
                })
                .map(TypeElement.class::cast)
                .collect(Collectors.toSet());
        Set<TypeElement> entities = roundEnv.getElementsAnnotatedWith(Table.class)
                .stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toSet());
        Set<TypeElement> cacheables = entities.stream()
                .filter(ele -> ele.getAnnotation(Cacheable.class) != null)
                .collect(Collectors.toSet());
        new EntitiesBuilder(processingEnv, entities).process();
        new QueriesBuilder(processingEnv, types).process();
        List<RelationshipInfo> relationshipInfos = new RelationshipBuilder(processingEnv, entities, types).process();
        if (!entities.isEmpty()) {
            buildDelegates(entities);
        }
        if (!types.isEmpty()) {
            buildQueries(types);
        }
        if (!cacheables.isEmpty()) {
            buildCacheables(cacheables);
        }
        if (!entities.isEmpty()) {
            buildRelationshipEvents(relationshipInfos);
        }
        return true;
    }

    private void buildRelationshipEvents(List<RelationshipInfo> relationshipInfos) {
        logger.info("Building RelationshipEvents Class"::toString);
        TypeSpec relationshipEvents = TypeSpec.classBuilder("RelationshipEvents")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(RelationshipService.class)
                .addField(relationshipMap(), "relationships", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(relationshipEventsConstructor(relationshipInfos))
                .addMethod(buildGetRelationships())
                .build();
        try {
            JavaFile.builder("io.jaorm.entity.relationship", relationshipEvents)
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .build().writeTo(processingEnv.getFiler());
        } catch (IOException ex) {
            throw new ProcessorException(ex);
        }
    }

    private MethodSpec buildGetRelationships() {
        return MethodSpec.overriding(MethodUtils.getMethod(processingEnv, "getRelationships", RelationshipService.class))
                .addStatement("return (Relationship<T>) this.relationships.get($L)", "arg0")
                .addAnnotation(
                        AnnotationSpec.builder(SuppressWarnings.class)
                            .addMember("value", "$S", "unchecked")
                            .build()
                )
                .build();
    }

    private MethodSpec relationshipEventsConstructor(List<RelationshipInfo> relationshipInfos) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addCode(relationshipEventsConstructorCode(relationshipInfos))
                .build();
    }

    private CodeBlock relationshipEventsConstructorCode(List<RelationshipInfo> relationshipInfos) {
        CodeBlock.Builder builder = CodeBlock.builder()
                .addStatement("$T map = new $T<>()", relationshipMap(), HashMap.class);
        for (RelationshipInfo info : relationshipInfos) {
            builder.addStatement("map.put($T.class, new $T<>($T.class))", info.getType(), Relationship.class, info.getType());
            for (RelationshipAccessor relationship : info.getRelationships()) {
                String events;
                if (relationship.getCascadeType().equals(CascadeType.ALL)) {
                    events = asVarArgs(EntityEventType.values());
                } else if (relationship.getCascadeType().equals(CascadeType.REMOVE)) {
                    events = asVarArgs(EntityEventType.REMOVE);
                } else if (relationship.getCascadeType().equals(CascadeType.PERSIST)) {
                    events = asVarArgs(EntityEventType.PERSIST);
                } else {
                    events = asVarArgs(EntityEventType.UPDATE);
                }
                builder.addStatement("map.get($T.class).add(new $T<>(e -> (($T)e).$L(), $L, $L, $L))",
                        info.getType(), Relationship.Node.class,
                        info.getType(),
                        relationship.getGetter().getSimpleName(),
                        relationship.getReturnTypeDefinition().isOptional() ? "true" : "false",
                        relationship.getReturnTypeDefinition().isCollection() ? "true" : "false",
                        events
                );
            }
        }

        return builder.addStatement("this.relationships = $T.unmodifiableMap(map)", Collections.class)
                .build();
    }

    private String asVarArgs(EntityEventType... types) {
        return Stream.of(types)
                .map(Enum::name)
                .map(s -> EntityEventType.class.getName() + "." + s)
                .collect(Collectors.joining(","));
    }

    private TypeName relationshipMap() {
        return ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),
                ParameterizedTypeName.get(ClassName.get(Relationship.class), WildcardTypeName.subtypeOf(Object.class))
        );
    }

    private void buildCacheables(Set<TypeElement> cacheables) {
        logger.info("Building Caches Class"::toString);
        TypeSpec caches = TypeSpec.classBuilder("Caches")
                .addModifiers(Modifier.PUBLIC)
                .superclass(CacheService.class)
                .addField(cachesMap(), "caches", Modifier.PRIVATE, Modifier.FINAL)
                .addField(cacheablesSet(), "cacheables", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(cachesConstructor(cacheables))
                .addMethods(buildCacheService())
                .build();
        try {
            JavaFile.builder("io.jaorm.cache", caches)
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .build().writeTo(processingEnv.getFiler());
        } catch (IOException ex) {
            throw new ProcessorException(ex);
        }
    }

    private Iterable<MethodSpec> buildCacheService() {
        MethodSpec activeCache = MethodSpec.overriding(MethodUtils.getMethod(processingEnv, "isCacheActive", CacheService.class))
                .addStatement("return true")
                .build();
        MethodSpec getCaches = MethodSpec.overriding(MethodUtils.getMethod(processingEnv, "getCaches", CacheService.class))
                .returns(cachesMap())
                .addStatement("return this.caches")
                .build();
        MethodSpec isCacheable = MethodSpec.overriding(MethodUtils.getMethod(processingEnv, "isCacheable", CacheService.class))
                .addStatement("return this.cacheables.contains(arg0)")
                .build();
        return Arrays.asList(activeCache, getCaches, isCacheable);
    }

    private TypeName cacheablesSet() {
        return ParameterizedTypeName.get(ClassName.get(Set.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)));
    }

    private MethodSpec cachesConstructor(Set<TypeElement> cacheables) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.caches = new $T<>()", ConcurrentHashMap.class)
                .addStatement("this.cacheables = new $T<>()", HashSet.class);
        cacheables.forEach(ele -> builder.addStatement("this.cacheables.add($T.class)", ele));
        return builder.build();
    }

    private TypeName cachesMap() {
        return ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),
                ParameterizedTypeName.get(ClassName.get(EntityCache.class), WildcardTypeName.subtypeOf(Object.class))
        );
    }

    private void buildQueries(Set<TypeElement> types) {
        logger.info("Building Queries Class"::toString);
        TypeSpec queries = TypeSpec.classBuilder("Queries")
                .addModifiers(Modifier.PUBLIC)
                .superclass(QueriesService.class)
                .addField(queriesMap(), "queries", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(queriesConstructor(types))
                .addMethod(buildGetQueries())
                .build();
        try {
            JavaFile.builder(JAORM_PACKAGE, queries)
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .build().writeTo(processingEnv.getFiler());
        } catch (IOException ex) {
            throw new ProcessorException(ex);
        }
    }

    private MethodSpec buildGetQueries() {
        return MethodSpec.overriding(MethodUtils.getMethod(processingEnv, "getQueries", QueriesService.class))
                .addStatement("return this.queries")
                .build();
    }

    private MethodSpec queriesConstructor(Set<TypeElement> types) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement(MAP_FORMAT, queriesMap(), HashMap.class);
        types.forEach(type -> {
            TypeElement baseType;
            if (QueriesBuilder.isBaseDao(type)) {
                baseType = processingEnv.getElementUtils().getTypeElement(QueriesBuilder.getBaseDaoGeneric(type));
            } else {
                baseType = processingEnv.getElementUtils().getTypeElement(Object.class.getName());
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

    private void buildDelegates(Set<TypeElement> types) {
        logger.info("Building Delegates Class"::toString);
        TypeSpec delegates = TypeSpec.classBuilder("Delegates")
                .addModifiers(Modifier.PUBLIC)
                .superclass(DelegatesService.class)
                .addField(delegatesMap(), "delegates", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(delegateConstructor(types))
                .addMethod(buildGetDelegates())
                .build();
        try {
            JavaFile.builder(JAORM_PACKAGE, delegates)
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .build().writeTo(processingEnv.getFiler());
        } catch (IOException ex) {
            throw new ProcessorException(ex);
        }
    }

    private MethodSpec buildGetDelegates() {
        return MethodSpec.overriding(MethodUtils.getMethod(processingEnv, "getDelegates", DelegatesService.class))
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

    private MethodSpec delegateConstructor(Set<TypeElement> types) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement(MAP_FORMAT, delegatesMap(), HashMap.class);
        types.forEach(type -> builder.addStatement("values.put($L.class, $LDelegate::new)", type.getQualifiedName(), type.getQualifiedName()));
        builder.addStatement("this.delegates = values");
        return builder.build();
    }
}
