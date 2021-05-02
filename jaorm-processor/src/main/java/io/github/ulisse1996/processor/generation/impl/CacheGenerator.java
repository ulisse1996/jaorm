package io.github.ulisse1996.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.processor.generation.Generator;
import io.github.ulisse1996.annotation.Cacheable;
import io.github.ulisse1996.annotation.Table;
import io.github.ulisse1996.cache.EntityCache;
import io.github.ulisse1996.logger.JaormLogger;
import io.github.ulisse1996.processor.util.GeneratedFile;
import io.github.ulisse1996.processor.util.ProcessorUtils;
import io.github.ulisse1996.spi.CacheService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CacheGenerator extends Generator {

    private static final JaormLogger logger = JaormLogger.getLogger(CacheGenerator.class);

    public CacheGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<TypeElement> entities = roundEnvironment.getElementsAnnotatedWith(Table.class)
                .stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toList());
        Set<TypeElement> cacheables = entities.stream()
                .filter(ele -> ele.getAnnotation(Cacheable.class) != null)
                .collect(Collectors.toSet());
        if (!cacheables.isEmpty()) {
            generateCaches(cacheables);
        } else {
            logger.debug("Skipping Caches Generation"::toString);
        }
    }

    private void generateCaches(Set<TypeElement> cacheables) {
        TypeSpec caches = TypeSpec.classBuilder("Caches")
                .addModifiers(Modifier.PUBLIC)
                .superclass(CacheService.class)
                .addField(cachesMap(), "caches", Modifier.PRIVATE, Modifier.FINAL)
                .addField(cacheablesSet(), "cacheables", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(cachesConstructor(cacheables))
                .addMethods(buildCacheService())
                .build();
        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, caches, ""));
    }

    private Iterable<MethodSpec> buildCacheService() {
        MethodSpec activeCache = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "isCacheActive", CacheService.class))
                .addStatement("return true")
                .build();
        MethodSpec getCaches = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getCaches", CacheService.class))
                .returns(cachesMap())
                .addStatement("return this.caches")
                .build();
        MethodSpec isCacheable = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "isCacheable", CacheService.class))
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
}
