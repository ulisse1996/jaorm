package io.github.ulisse1996.jaorm.external.support.mock;

import io.github.ulisse1996.jaorm.external.LombokElementMock;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.Set;

public class MockParameter extends LombokElementMock implements VariableElement {

    private final ExecutableElement method;

    public MockParameter(ExecutableElement method, Element element) {
        super(element);
        this.method = method;
    }

    @Override
    public Object getConstantValue() {
        return null;
    }

    @Override
    public TypeMirror asType() {
        return element.asType();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PARAMETER;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public Name getSimpleName() {
        return element.getSimpleName();
    }

    @Override
    public Element getEnclosingElement() {
        return method;
    }
}
