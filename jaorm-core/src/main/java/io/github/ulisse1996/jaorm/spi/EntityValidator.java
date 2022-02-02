package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.validation.ValidationResult;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.List;
import java.util.ServiceConfigurationError;

public abstract class EntityValidator {

    private static final Singleton<EntityValidator> INSTANCE = Singleton.instance();

    public static synchronized EntityValidator getInstance() {
        if (!INSTANCE.isPresent()) {
            try {
                INSTANCE.set(ServiceFinder.loadService(EntityValidator.class));
            } catch (Exception | ServiceConfigurationError ex) {
                INSTANCE.set(NoOp.INSTANCE);
            }
        }

        return INSTANCE.get();
    }

    public abstract boolean isActive();
    public abstract <R> List<ValidationResult<R>> validate(R entity);

    protected static class NoOp extends EntityValidator {

        protected static final NoOp INSTANCE = new NoOp();

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public <R> List<ValidationResult<R>> validate(R entity) {
            throw new UnsupportedOperationException("Can't validate entity !");
        }
    }
}
