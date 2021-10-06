package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.*;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.entity.converter.ConverterPair;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.spi.ConverterService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConverterGenerator extends Generator {

    public ConverterGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<ConverterInfo> conversions = roundEnvironment.getElementsAnnotatedWith(Converter.class)
                .stream()
                .map(element -> {
                    ConverterInfo converterInfo = new ConverterInfo();
                    converterInfo.converterInstance = ProcessorUtils.getConverterCaller(processingEnvironment,
                            (VariableElement) element);
                    List<TypeElement> converterTypes = ProcessorUtils.getConverterTypes(processingEnvironment,
                            (VariableElement) element);
                    converterInfo.beforeClass = converterTypes.get(0);
                    converterInfo.afterClass = converterTypes.get(1);
                    return converterInfo;
                }).collect(Collectors.toList());
        if (!conversions.isEmpty()) {
            generateConverters(conversions);
        }
    }

    private void generateConverters(List<ConverterInfo> conversions) {
        debugMessage("Generating Converters");
        TypeSpec converters = TypeSpec.classBuilder("Converters")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ConverterService.class)
                .addField(converterMap(), "convertersMap", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(converterConstructor(conversions))
                .addMethod(buildGetConverters())
                .build();
        ProcessorUtils.generate(processingEnvironment,
                new GeneratedFile(JAORM_PACKAGE, converters, ""));
    }

    private MethodSpec buildGetConverters() {
        return MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getConverters", ConverterService.class))
                .addStatement("return this.convertersMap")
                .build();
    }

    private MethodSpec converterConstructor(List<ConverterInfo> conversions) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$T values = new $T<>()", converterMap(), HashMap.class);
        conversions.forEach(conv -> builder.addStatement("values.put($L.class, new $T<>($L.class, $L))",
                conv.afterClass, ConverterPair.class, conv.beforeClass, conv.converterInstance));
        builder.addStatement("this.convertersMap = values");
        return builder.build();
    }

    private TypeName converterMap() {
        return ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),
                ParameterizedTypeName.get(ClassName.get(ConverterPair.class),
                        WildcardTypeName.subtypeOf(Object.class),
                        WildcardTypeName.subtypeOf(Object.class)
                )
        );
    }

    private static class ConverterInfo {
        TypeElement beforeClass;
        TypeElement afterClass;
        String converterInstance;
    }
}
