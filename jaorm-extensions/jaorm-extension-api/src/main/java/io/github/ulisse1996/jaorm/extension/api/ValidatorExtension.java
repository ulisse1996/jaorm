package io.github.ulisse1996.jaorm.extension.api;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

public interface ValidatorExtension {

    Set<Class<? extends Annotation>> getSupported();
    void validate(Set<Element> elements, ProcessingEnvironment processingEnvironment);
    void validateSql(String sql, ProcessingEnvironment processingEnvironment);
}
