package io.jaorm.processor.validation;

import io.jaorm.annotation.Table;
import io.jaorm.processor.validation.impl.EntityValidator;
import io.jaorm.processor.validation.impl.QueryValidator;
import io.jaorm.processor.validation.impl.RelationshipValidator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.util.List;

public abstract class Validator {

    protected final ProcessingEnvironment processingEnvironment;

    protected Validator(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public static Validator forType(ValidatorType type, ProcessingEnvironment processingEnvironment) {
        switch (type) {
            case QUERY:
                return new QueryValidator(processingEnvironment);
            case ENTITY:
                return new EntityValidator(processingEnvironment);
            case RELATIONSHIP:
                return new RelationshipValidator(processingEnvironment);
            default:
                throw new IllegalArgumentException("Can't find validator");
        }
    }

    public abstract void validate(List<? extends Element> annotated);
}
