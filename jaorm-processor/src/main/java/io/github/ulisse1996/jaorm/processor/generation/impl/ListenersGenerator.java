package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.ulisse1996.jaorm.annotation.GlobalListener;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.spi.provider.ListenerProvider;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListenersGenerator extends Generator {

    public ListenersGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<TypeElement> listeners = roundEnvironment.getElementsAnnotatedWith(GlobalListener.class)
                .stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toList());
        if (!listeners.isEmpty()) {
            generateEntityListener(listeners);
        }
    }

    private void generateEntityListener(List<TypeElement> listeners) {
        List<GeneratedFile> files = new ArrayList<>();
        for (TypeElement type : listeners) {
            TypeSpec entityListeners = TypeSpec.classBuilder(String.format("%sListenerProvider", type.getSimpleName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ListenerProvider.class)
                    .addMethod(
                            MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getEntityClass", ListenerProvider.class))
                                    .addStatement("return $T.class", type)
                                    .build()
                    ).build();
            GeneratedFile file = new GeneratedFile(getPackage(type), entityListeners, "");
            ProcessorUtils.generate(processingEnvironment, file);
        }

        ProcessorUtils.generateSpi(processingEnvironment, files, ListenerProvider.class);
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
    }
}
