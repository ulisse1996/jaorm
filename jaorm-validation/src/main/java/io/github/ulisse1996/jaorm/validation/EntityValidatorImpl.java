package io.github.ulisse1996.jaorm.validation;

import io.github.ulisse1996.jaorm.entity.validation.ValidationResult;
import io.github.ulisse1996.jaorm.spi.BeanProvider;
import io.github.ulisse1996.jaorm.spi.EntityValidator;

import javax.validation.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityValidatorImpl extends EntityValidator {

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public <R> List<ValidationResult<R>> validate(R entity) {
        BeanProvider provider = BeanProvider.getInstance();
        if (provider.isActive()) {
            // Delegate Validation if a custom Validator Bean is provided
            Optional<Validator> validator = provider.getOptBean(Validator.class);
            if (validator.isPresent()) {
                return validator.get().validate(entity)
                        .stream()
                        .map(this::toResult)
                        .collect(Collectors.toList());
            }
        }
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<R>> result = factory
                    .getValidator()
                    .validate(entity);
            return result.stream()
                    .map(this::toResult)
                    .collect(Collectors.toList());
        }
    }

    private <R> ValidationResult<R> toResult(ConstraintViolation<R> r) {
        return new ValidationResult<>(
                r.getMessage(),
                r.getRootBean(),
                r.getRootBeanClass(),
                r.getInvalidValue(),
                Optional.ofNullable(r.getPropertyPath())
                        .map(Path::toString)
                        .orElse(null)
        );
    }
}
