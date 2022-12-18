package io.github.ulisse1996.jaorm.extension.cdi;

import jakarta.enterprise.util.TypeLiteral;
import org.jboss.weld.inject.WeldInstance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Iterator;

public class CustomInstance implements WeldInstance<Object> {

    private final WeldInstance<Object> instance;

    public CustomInstance(WeldInstance<Object> instance) {
        this.instance = instance;
    }

    @Override
    public Handler<Object> getHandler() {
        return this.instance.getHandler();
    }

    @Override
    public Iterable<Handler<Object>> handlers() {
        return this.instance.handlers();
    }

    @Override
    public Comparator<Handler<?>> getPriorityComparator() {
        return this.instance.getPriorityComparator();
    }

    @Override
    public WeldInstance<Object> select(Annotation... qualifiers) {
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
    public <U> WeldInstance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return instance.select(subtype, qualifiers);
    }

    @Override
    public <X> WeldInstance<X> select(Type type, Annotation... annotations) {
        return null;
    }

    @Override
    public <U> WeldInstance<U> select(Class<U> subtype, Annotation... qualifiers) {
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
