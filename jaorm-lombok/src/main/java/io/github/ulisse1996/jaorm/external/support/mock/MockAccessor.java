package io.github.ulisse1996.jaorm.external.support.mock;

import io.github.ulisse1996.jaorm.external.LombokMock;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class MockAccessor implements LombokMock, ExecutableElement {

    protected final VariableElement element;
    private final boolean getter;

    protected MockAccessor(Element element, boolean getter) {
        this.element = (VariableElement) element;
        this.getter = getter;
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        return Collections.emptyList();
    }

    @Override
    public TypeMirror getReturnType() {
        return element.asType();
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public TypeMirror getReceiverType() {
        return element.getEnclosingElement().asType();
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
        return Collections.emptyList();
    }

    @Override
    public AnnotationValue getDefaultValue() {
        return null;
    }

    @Override
    public TypeMirror asType() {
        return element.asType();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.METHOD;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.singleton(Modifier.PUBLIC);
    }

    @Override
    public Name getSimpleName() {
        String fieldName = element.getSimpleName().toString();
        fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String name;
        if (this.getter) {
            if (isBoolean()) {
                name = "is" + fieldName;
            } else {
                name = "get" + fieldName;
            }
        } else {
            name = "set" + fieldName;
        }
        return new MockName(name);
    }

    private boolean isBoolean() {
        return element.asType().toString().contains("boolean")
                || element.asType().toString().contains("Boolean");
    }

    @Override
    public Element getEnclosingElement() {
        return element.getEnclosingElement();
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return Collections.emptyList();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return (A[]) new Annotation[0];
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return null;
    }
}
