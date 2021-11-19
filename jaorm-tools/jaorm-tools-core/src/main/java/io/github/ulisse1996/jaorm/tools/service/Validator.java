package io.github.ulisse1996.jaorm.tools.service;

import io.github.ulisse1996.jaorm.tools.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.tools.exception.QueryValidationException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public interface Validator {

    void validateEntities() throws EntityValidationException, IOException, SQLException, NoSuchAlgorithmException;
    void validateQueries() throws QueryValidationException, IOException, NoSuchAlgorithmException;
}
