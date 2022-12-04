package io.github.ulisse1996.jaorm.extension.cdi;

import org.jboss.weld.inject.WeldInstance;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.util.Iterator;

public class CustomInstance implements Instance<Object> {

    private final WeldInstance<Object> instance;

    public CustomInstance(WeldInstance<Object> instance) {
        this.instance = instance;
    }

    @Override
    public Instance<Object> select(Annotation... qualifiers) {
        return instance.select(qualifiers);
    }

    @Override
    public boolean isUnsatisfied() {
        return instance.isUnsatisfied();
    }

    @Override
    public boolean isAmbiguous() {
        return instance.isAmbiguous();
    }

    @Override
    public void destroy(Object instance) {
        this.instance.destroy(instance);
    }

    @Override
    public <U> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return instance.select(subtype, qualifiers);
    }

    @Override
    public <U> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return instance.select(subtype, qualifiers);
    }

    @Override
    public Iterator<Object> iterator() {
        return instance.iterator();
    }

    @Override
    public Object get() {
        return instance.get();
    }
}
