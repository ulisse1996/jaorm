package io.jaorm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceFinder {

    private ServiceFinder() {}

    public static <R> Iterable<R> loadServices(Class<R> klass) {
        List<ClassLoader> classLoaders = Stream.of(
                klass.getClassLoader(), Thread.currentThread().getContextClassLoader(), ServiceFinder.class.getClassLoader())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<R> values = new ArrayList<>();
        for (ClassLoader loader : classLoaders) {
            Iterator<R> iterator = ServiceLoader.load(klass, loader).iterator();
            if (iterator.hasNext()) {
                iterator.forEachRemaining(values::add);
            }
        }

        return values;
    }

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

        throw new IllegalArgumentException("Can't find service of " + klass);
    }
}
