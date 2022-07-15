package io.github.ulisse1996.jaorm.integration.test.cdi.inject;

import javax.enterprise.util.AnnotationLiteral;

public class MyIdentifierLiteral extends AnnotationLiteral<MyIdentifier> implements MyIdentifier {

    private final Class<?> entityClass;

    public MyIdentifierLiteral(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Class<?> value() {
        return entityClass;
    }
}
