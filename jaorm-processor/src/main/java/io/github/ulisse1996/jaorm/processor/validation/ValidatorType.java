package io.github.ulisse1996.jaorm.processor.validation;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.annotation.Relationship;
import io.github.ulisse1996.jaorm.annotation.Table;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public enum ValidatorType {
    QUERY(Query.class, Dao.class),
    ENTITY(Table.class),
    RELATIONSHIP(Relationship.class);

    private final List<Class<? extends Annotation>> supported;

    @SafeVarargs
    ValidatorType(Class<? extends Annotation>... annotations) {
        this.supported = Arrays.asList(annotations);
    }

    public List<Class<? extends Annotation>> getSupported() {
        return supported;
    }
}
