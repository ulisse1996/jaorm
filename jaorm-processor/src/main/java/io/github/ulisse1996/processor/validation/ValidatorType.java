package io.github.ulisse1996.processor.validation;

import io.github.ulisse1996.annotation.Dao;
import io.github.ulisse1996.annotation.Query;
import io.github.ulisse1996.annotation.Relationship;
import io.github.ulisse1996.annotation.Table;

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
