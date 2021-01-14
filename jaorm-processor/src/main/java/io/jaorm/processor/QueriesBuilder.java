package io.jaorm.processor;

import com.squareup.javapoet.MethodSpec;
import io.jaorm.processor.annotation.Query;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

public class QueriesBuilder {

    private final Set<TypeElement> types;

    public QueriesBuilder(Set<TypeElement> types) {
        this.types = types;
    }

    public void process(ProcessingEnvironment processingEnvironment) {
        for (TypeElement query : types) {
            Set<MethodSpec> methods = query.getEnclosedElements()
                    .stream()
                    .filter(ele -> ele.getAnnotation(Query.class) != null)
                    .map(ExecutableElement.class::cast)
                    .map(this::buildImpl)
                    .collect(Collectors.toSet());
        }
    }

    private MethodSpec buildImpl(ExecutableElement executableElement) {
        return MethodSpec.overriding(executableElement)
                .build();
    }
}
