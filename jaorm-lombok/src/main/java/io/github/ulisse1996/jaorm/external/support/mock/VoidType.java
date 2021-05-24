package io.github.ulisse1996.jaorm.external.support.mock;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class VoidType implements NoType {

    public static final VoidType TYPE = new VoidType();

    private VoidType() {}

    @Override
    public TypeKind getKind() {
        return TypeKind.VOID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return (R) TypeName.VOID;
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
}
