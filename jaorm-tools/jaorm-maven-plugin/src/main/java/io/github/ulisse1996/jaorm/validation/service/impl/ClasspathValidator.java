package io.github.ulisse1996.jaorm.validation.service.impl;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.validation.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.validation.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.validation.model.EntityMetadata;
import io.github.ulisse1996.jaorm.validation.service.AbstractValidator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class ClasspathValidator extends AbstractValidator {

    private final ClassLoader classLoader;

    public ClasspathValidator(ClassLoader classLoader, String projectRoot, ConnectionInfo connectionInfo) throws IOException {
        super(projectRoot, connectionInfo);
        this.classLoader = classLoader;
    }

    @Override
    public void validate() throws EntityValidationException, SQLException {
        for (DelegatesService delegatesService : ServiceLoader.load(DelegatesService.class, classLoader)) {
            for (Map.Entry<Class<?>, Supplier<? extends EntityDelegate<?>>> entry : delegatesService.getDelegates().entrySet()) {
                EntityMetadata metadata = new EntityMetadata(entry);
                validate(entry.getKey().getSimpleName(), metadata, metadata.getTable());
            }
        }
    }
}
