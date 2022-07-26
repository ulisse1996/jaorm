package io.github.ulisse1996.jaorm.processor;

import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ExtensionLoader {

    private ExtensionLoader() {}

    public static List<ExtensionManager> loadValidationExtensions(ProcessingEnvironment environment) {
        // Compiler use a custom classloader, we need to load klass in different mode
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Optional<Class<?>> aClass = tryLoad(contextClassLoader, "io.github.ulisse1996.jaorm.extension.api.ValidatorExtension");
            if (!aClass.isPresent()) {
                environment.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "Skipping custom validation for missing ValidatorExtension"
                );
                return Collections.emptyList();
            }
            return StreamSupport.stream(
                    ServiceLoader.load(aClass.get(), contextClassLoader).spliterator(), false
            ).map(ExtensionManager::new)
            .collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ProcessorException(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static Optional<Class<?>> tryLoad(ClassLoader contextClassLoader, String name) {
        try {
            return Optional.of(contextClassLoader.loadClass(name));
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }
    }

    public static class ExtensionManager {

        private final Object delegate;
        private final Map<String, Method> cache;

        private ExtensionManager(Object o) {
            this.delegate = o;
            this.cache = new ConcurrentHashMap<>();
        }

        public void executeValidation(Set<Element> elements, ProcessingEnvironment processingEnvironment) {
            Method method = this.loadMethod("validate");
            try {
                method.invoke(this.delegate, elements, processingEnvironment);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ProcessorException(e.getMessage(), e);
            }
        }

        public void executeValidation(String sql, ProcessingEnvironment processingEnvironment) {
            Method method = this.loadMethod("validateSql");
            try {
                method.invoke(this.delegate, sql, processingEnvironment);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ProcessorException(e.getMessage(), e);
            }
        }

        private Method loadMethod(String name) {
            if (cache.containsKey(name)) {
                return cache.get(name);
            }
            try {
                Class<?> current = this.delegate.getClass();
                while (current != null) {
                    Method[] methods = current.getDeclaredMethods();
                    for (Method m : methods) {
                        if (m.getName().equalsIgnoreCase(name)) {
                            cache.put(name, m);
                            return m;
                        }
                    }
                    current = current.getSuperclass();
                }

                throw new IllegalArgumentException(String.format("Can't find method %s!", name));
            } catch (Exception ex) {
                throw new ProcessorException(ex.getMessage());
            }
        }

        @SuppressWarnings("unchecked")
        public Set<Class<? extends Annotation>> getSupported() {
            Method method = this.loadMethod("getSupported");
            try {
                return (Set<Class<? extends Annotation>>) method.invoke(this.delegate);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new ProcessorException(ex.getMessage(), ex);
            }
        }
    }
}
