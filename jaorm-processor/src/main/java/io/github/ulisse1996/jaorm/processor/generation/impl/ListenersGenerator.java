package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.jaorm.annotation.GlobalListener;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.spi.ListenersService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        TypeSpec entityListeners = TypeSpec.classBuilder("EntityListeners" + ProcessorUtils.randomIdentifier())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ListenersService.class)
                .addField(classSet(), "classes", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(listenersConstructor(listeners))
                .addMethod(buildGetClasses())
                .build();
        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, entityListeners, ""));
        ProcessorUtils.generateSpi(
                processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, entityListeners, ""),
                ListenersService.class
        );
    }

    private MethodSpec buildGetClasses() {
        return MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getEventClasses", ListenersService.class))
                .addStatement("return this.classes")
                .build();
    }

    private MethodSpec listenersConstructor(List<TypeElement> listeners) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T values = new $T<>()", classSet(), HashSet.class);
        listeners.forEach(type -> builder.addStatement("values.add($T.class)", type));
        builder.addStatement("this.classes = values");
        return builder.build();
    }

    private ParameterizedTypeName classSet() {
        return ParameterizedTypeName.get(ClassName.get(Set.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)));
    }
}
