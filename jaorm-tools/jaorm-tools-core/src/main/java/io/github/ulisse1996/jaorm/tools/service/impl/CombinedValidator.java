package io.github.ulisse1996.jaorm.tools.service.impl;

import io.github.ulisse1996.jaorm.tools.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.tools.exception.QueryValidationException;
import io.github.ulisse1996.jaorm.tools.service.Validator;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class CombinedValidator implements Validator {

    private final List<Validator> validators;

    public CombinedValidator(Validator... validators) {
        this.validators = Arrays.asList(validators);
    }

    @Override
    public void validateEntities() throws EntityValidationException, SQLException, IOException, NoSuchAlgorithmException {
        for (Validator validator : validators) {
            validator.validateEntities();
        }
    }

    @Override
    public void validateQueries() throws QueryValidationException, IOException, NoSuchAlgorithmException {
        for (Validator validator : validators) {
            validator.validateQueries();
        }
    }
}
