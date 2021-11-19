package io.github.ulisse1996.jaorm.tools.service.impl;

import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.tools.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.tools.exception.QueryValidationException;
import io.github.ulisse1996.jaorm.tools.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.tools.model.EntityMetadata;
import io.github.ulisse1996.jaorm.tools.service.AbstractValidator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ClasspathValidator extends AbstractValidator {

    private final ClassLoader classLoader;
    private final boolean skipCache;

    public ClasspathValidator(ClassLoader classLoader, String projectRoot, ConnectionInfo connectionInfo, boolean skipCache) throws IOException {
        super(projectRoot, connectionInfo);
        this.classLoader = classLoader;
        this.skipCache = skipCache;
    }

    @Override
    public void validateEntities() throws EntityValidationException, SQLException, IOException, NoSuchAlgorithmException {
        try {
            getLog().info("Validating Entities using Classpath"::toString);
            for (DelegatesService delegatesService : ServiceLoader.load(DelegatesService.class, classLoader)) {
                for (Map.Entry<Class<?>, Supplier<? extends EntityDelegate<?>>> entry : delegatesService.getDelegates().entrySet()) {
                    String path = getPath(entry.getKey());
                    String current = cache.getCurrentHash(path);
                    String calculated = cache.calculateHash(path);
                    if (current.equalsIgnoreCase(calculated) && !this.skipCache) {
                        getLog().info(() -> String.format("Skipping calculation for File %s", path));
                        continue;
                    }
                    EntityMetadata metadata = new EntityMetadata(entry);
                    validate(entry.getKey().getSimpleName(), metadata, metadata.getTable());
                    cache.updateHash(path, calculated);
                }
            }
        } finally {
            if (this.classLoader instanceof URLClassLoader) {
                ((URLClassLoader) this.classLoader).close();
            }
        }
    }

    @Override
    public void validateQueries() throws QueryValidationException, IOException {
        try {
            getLog().info("Validating SQL using Classpath"::toString);
            for (QueriesService queriesService : ServiceLoader.load(QueriesService.class, classLoader)) {
                for (Class<?> klass : queriesService.getQueries().keySet()) {
                    getLog().info(() -> String.format("Checking %s", klass));
                    String path = getPath(klass);
                    String current = cache.getCurrentHash(path);
                    String calculated = cache.calculateHash(path);
                    if (current.equalsIgnoreCase(calculated) && !this.skipCache) {
                        getLog().info(() -> String.format("Skipping calculation for File %s", path));
                        continue;
                    }
                    List<Method> methods = Arrays.stream(klass.getDeclaredMethods())
                            .filter(m -> m.isAnnotationPresent(Query.class))
                            .collect(Collectors.toList());
                    for (Method method : methods) {
                        Query query = method.getAnnotation(Query.class);
                        validateQuery(query.sql(), query.noArgs());
                    }
                    cache.updateHash(path, calculated);
                }
            }
        } catch (Exception ex) {
            throw new QueryValidationException(ex);
        } finally {
            if (this.classLoader instanceof URLClassLoader) {
                ((URLClassLoader) this.classLoader).close();
            }
        }
    }

    private String getPath(Class<?> key) {
        URL resource = this.classLoader.getResource(key.getName().replace(".", "/") + ".class");
        return Objects.requireNonNull(resource).toString();
    }
}
