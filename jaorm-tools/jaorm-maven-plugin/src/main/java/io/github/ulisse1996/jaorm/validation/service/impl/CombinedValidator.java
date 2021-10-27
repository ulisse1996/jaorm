package io.github.ulisse1996.jaorm.validation.service.impl;

import io.github.ulisse1996.jaorm.validation.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.validation.service.Validator;

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
    public void validate() throws EntityValidationException, SQLException, IOException, NoSuchAlgorithmException {
        for (Validator validator : validators) {
            validator.validate();
        }
    }
}
