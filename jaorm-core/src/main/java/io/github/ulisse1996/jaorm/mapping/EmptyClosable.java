package io.github.ulisse1996.jaorm.mapping;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public interface EmptyClosable extends Closeable {

    InvocationHandler DEFAULT = (proxy, method, args) -> {
        if ("equals".equalsIgnoreCase(method.getName())) {
            return false;
        } else if ("hashCode".equalsIgnoreCase(method.getName())) {
            return 31;
        } else if ("toString".equalsIgnoreCase(method.getName())) {
            return EmptyClosable.class.toString();
        }

        return null;
    };

    @SuppressWarnings("unchecked")
    static <R extends AutoCloseable> R instance(Class<R> klass) {
        return (R) Proxy.newProxyInstance(klass.getClassLoader(), new Class[] {klass}, DEFAULT);
    }
}
