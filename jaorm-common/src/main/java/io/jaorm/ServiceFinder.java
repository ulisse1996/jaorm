package io.jaorm;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceFinder {

    private ServiceFinder() {}

    public static <R> R loadService(Class<R> klass) {
        List<ClassLoader> classLoaders = Stream.of(
                klass.getClassLoader(), Thread.currentThread().getContextClassLoader(), ServiceFinder.class.getClassLoader())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (ClassLoader loader : classLoaders) {
            Iterator<R> iterator = ServiceLoader.load(klass, loader).iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        }

        throw new IllegalArgumentException("Can't find service of class : " + klass);
    }
}
