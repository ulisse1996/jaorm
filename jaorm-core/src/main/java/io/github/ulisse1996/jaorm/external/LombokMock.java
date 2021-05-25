package io.github.ulisse1996.jaorm.external;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public abstract class LombokMock implements AnnotatedConstruct {

    protected final Element element;

    protected LombokMock(Element element) {
        this.element = element;
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
