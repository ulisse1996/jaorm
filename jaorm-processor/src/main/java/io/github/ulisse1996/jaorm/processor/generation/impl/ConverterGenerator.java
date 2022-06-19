package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.spi.provider.ConverterProvider;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.*;
import java.util.stream.Collectors;

public class ConverterGenerator extends Generator {

    public ConverterGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        Set<ConverterInfo> conversions = roundEnvironment.getElementsAnnotatedWith(Converter.class)
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
                }).collect(Collectors.toSet());
        if (!conversions.isEmpty()) {
            generateConverters(conversions);
        }
    }

    private void generateConverters(Set<ConverterInfo> conversions) {
        debugMessage("Generating Converters");
        List<GeneratedFile> files = new ArrayList<>();
        for (ConverterInfo info : conversions) {
            TypeSpec converters = TypeSpec.classBuilder(String.format("%sProvider", info.afterClass.getSimpleName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ConverterProvider.class)
                    .addMethods(getConvertersMethods(info))
                    .build();
            GeneratedFile file = new GeneratedFile(JAORM_PACKAGE, converters, "");
            ProcessorUtils.generate(processingEnvironment, file);
            files.add(file);
        }

        ProcessorUtils.generateSpi(processingEnvironment, files, ConverterProvider.class);
    }

    private Iterable<MethodSpec> getConvertersMethods(ConverterInfo info) {
        MethodSpec from = MethodSpec.overriding(ProcessorUtils.getMethod(this.processingEnvironment, "from", ConverterProvider.class))
                .addStatement("return $L.class", info.beforeClass)
                .build();

        MethodSpec to = MethodSpec.overriding(ProcessorUtils.getMethod(this.processingEnvironment, "to", ConverterProvider.class))
                .addStatement("return $L.class", info.afterClass)
                .build();

        MethodSpec converter = MethodSpec.overriding(ProcessorUtils.getMethod(this.processingEnvironment, "converter", ConverterProvider.class))
                .addStatement("return $L", info.converterInstance)
                .build();

        return Arrays.asList(from, to, converter);
    }

    private static class ConverterInfo {
        TypeElement beforeClass;
        TypeElement afterClass;
        String converterInstance;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConverterInfo that = (ConverterInfo) o;
            return Objects.equals(beforeClass, that.beforeClass) && Objects.equals(afterClass, that.afterClass)
                    && Objects.equals(converterInstance, that.converterInstance);
        }

        @Override
        public int hashCode() {
            return Objects.hash(beforeClass, afterClass, converterInstance);
        }
    }
}
