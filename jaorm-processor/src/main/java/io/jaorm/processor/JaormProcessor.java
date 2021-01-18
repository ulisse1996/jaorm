package io.jaorm.processor;

import com.squareup.javapoet.*;
import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.processor.annotation.Query;
import io.jaorm.processor.annotation.Table;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.util.MethodUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("io.jaorm.processor.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JaormProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> queries = roundEnv.getElementsAnnotatedWith(Query.class);
        Set<TypeElement> types = queries.stream()
                .map(Element::getEnclosingElement)
                .map(TypeElement.class::cast)
                .collect(Collectors.toSet());
        Set<TypeElement> entities = roundEnv.getElementsAnnotatedWith(Table.class)
                .stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toSet());
        try {
            new EntitiesBuilder(processingEnv, entities).process();
            new QueriesBuilder(processingEnv, types).process();
            if (!entities.isEmpty()) {
                buildDelegates(entities);
            }
        } catch (Exception ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
            return false;
        }
        return true;
    }

    private void buildDelegates(Set<TypeElement> types) {
        TypeSpec delegates = TypeSpec.classBuilder("Delegates")
                .addModifiers(Modifier.PUBLIC)
                .superclass(DelegatesService.class)
                .addField(delegatesMap(), "delegates", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(delegateConstructor(types))
                .addMethod(buildGetDelegates())
                .build();
        try {
            JavaFile.builder("io.jaorm.entity", delegates)
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
                .addModifiers(Modifier.PRIVATE)
                .addStatement("$T values = new $T<>()", delegatesMap(), HashMap.class);
        types.forEach(type -> builder.addStatement("values.put($L.class, $LDelegate::new)", type.getQualifiedName(), type.getQualifiedName()));
        builder.addStatement("this.delegates = values");
        return builder.build();
    }
}
