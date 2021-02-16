package io.jaorm.processor.util;

import io.jaorm.processor.annotation.CascadeType;

import javax.lang.model.element.ExecutableElement;

public class RelationshipAccessor {

    private final ReturnTypeDefinition returnTypeDefinition;
    private final ExecutableElement getter;
    private final CascadeType cascadeType;

    public RelationshipAccessor(ReturnTypeDefinition returnTypeDefinition, ExecutableElement getter, CascadeType cascadeType) {
        this.returnTypeDefinition = returnTypeDefinition;
        this.getter = getter;
        this.cascadeType = cascadeType;
    }

    public ExecutableElement getGetter() {
        return getter;
    }

    public ReturnTypeDefinition getReturnTypeDefinition() {
        return returnTypeDefinition;
    }

    public CascadeType getCascadeType() {
        return cascadeType;
    }
}
