package io.github.ulisse1996.jaorm.exception;

import io.github.ulisse1996.jaorm.entity.validation.ValidationResult;

import java.util.List;

public class JaormValidationException extends RuntimeException {

    private final transient List<ValidationResult<Object>> results;

    public JaormValidationException(List<ValidationResult<Object>> results) {
        this.results = results;
    }

    public List<ValidationResult<Object>> getResults() {
        return results;
    }
}
