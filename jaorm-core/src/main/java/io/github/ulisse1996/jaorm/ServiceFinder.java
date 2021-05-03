package io.github.ulisse1996.jaorm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ServiceFinder {

    private ServiceFinder() {}

    public static <R> Iterable<R> loadServices(Class<R> klass) {
        Set<Class<?>> loadedClasses = new HashSet<>();
        List<ClassLoader> classLoaders = Stream.of(
                klass.getClassLoader(), Thread.currentThread().getContextClassLoader(), ServiceFinder.class.getClassLoader())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<R> values = new ArrayList<>();
        for (ClassLoader loader : classLoaders) {
            Iterator<R> iterator = ServiceLoader.load(klass, loader).iterator();
            if (iterator.hasNext()) {
                List<R> classes = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(iterator, Spliterator.SIZED), false)
                        .filter(el -> !loadedClasses.contains(el.getClass()))
                        .collect(Collectors.toList());
                loadedClasses.addAll(classes.stream().map(Object::getClass).collect(Collectors.toList()));
                values.addAll(classes);
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
