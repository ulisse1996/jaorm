package io.github.ulisse1996.jaorm.validation.service.impl;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.validation.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.validation.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.validation.model.EntityMetadata;
import io.github.ulisse1996.jaorm.validation.service.AbstractValidator;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class ClasspathValidator extends AbstractValidator {

    private final ClassLoader classLoader;

    public ClasspathValidator(ClassLoader classLoader, String projectRoot, ConnectionInfo connectionInfo) throws IOException {
        super(projectRoot, connectionInfo);
        this.classLoader = classLoader;
    }

    @Override
    public void validate() throws EntityValidationException, SQLException, IOException, NoSuchAlgorithmException {
        try {
            for (DelegatesService delegatesService : ServiceLoader.load(DelegatesService.class, classLoader)) {
                for (Map.Entry<Class<?>, Supplier<? extends EntityDelegate<?>>> entry : delegatesService.getDelegates().entrySet()) {
                    String path = getPath(entry.getKey());
                    String current = cache.getCurrentHash(path);
                    String calculated = cache.calculateHash(path);
                    if (current.equalsIgnoreCase(calculated)) {
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

    private String getPath(Class<?> key) {
        URL resource = this.classLoader.getResource(key.getName().replace(".", "/") + ".class");
        return Objects.requireNonNull(resource).toString();
    }
}
