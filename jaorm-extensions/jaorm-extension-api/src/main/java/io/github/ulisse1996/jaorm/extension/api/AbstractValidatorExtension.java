package io.github.ulisse1996.jaorm.extension.api;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.Set;

public abstract class AbstractValidatorExtension implements ValidatorExtension {

    @Override
    public void validate(Set<Element> elements, ProcessingEnvironment processingEnvironment) {}

    @Override
    public void validateSql(String sql, ProcessingEnvironment processingEnvironment) {}
}
