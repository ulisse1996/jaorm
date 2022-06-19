package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.ulisse1996.jaorm.annotation.Cacheable;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.spi.provider.CacheActivator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CacheGenerator extends Generator {

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
            debugMessage("Skipping Caches Generation");
        }
    }

    private void generateCaches(Set<TypeElement> cacheables) {
        List<GeneratedFile> files = new ArrayList<>();
        for (TypeElement type : cacheables) {
            TypeSpec caches = TypeSpec.classBuilder(String.format("%sCacheActivator", type.getSimpleName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(CacheActivator.class)
                    .addMethod(
                            MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getEntityClass", CacheActivator.class))
                                    .addStatement("return $T.class", type)
                                    .build()
                    )
                    .build();
            GeneratedFile file = new GeneratedFile(getPackage(type), caches, "");
            ProcessorUtils.generate(processingEnvironment, file);
            files.add(file);
        }

        ProcessorUtils.generateSpi(processingEnvironment, files, CacheActivator.class);
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
    }
}
