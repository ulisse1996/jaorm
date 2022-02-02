package io.github.ulisse1996.jaorm.entity.validation;

public class ValidationResult<R> {

    private final String message;
    private final R entity;
    private final Class<R> entityClass;
    private final Object invalidValue;
    private final String propertyPath;

    public ValidationResult(String message, R entity, Class<R> entityClass,
                            Object invalidValue, String propertyPath) {
        this.message = message;
        this.entity = entity;
        this.entityClass = entityClass;
        this.invalidValue = invalidValue;
        this.propertyPath = propertyPath;
    }

    public String getMessage() {
        return message;
    }

    public R getEntity() {
        return entity;
    }

    public Class<R> getEntityClass() {
        return entityClass;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    public String getPropertyPath() {
        return propertyPath;
    }
}
