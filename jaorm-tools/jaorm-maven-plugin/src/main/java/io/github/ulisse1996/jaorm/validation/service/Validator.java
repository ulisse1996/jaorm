package io.github.ulisse1996.jaorm.validation.service;

import io.github.ulisse1996.jaorm.validation.exception.EntityValidationException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public interface Validator {

    void validate() throws EntityValidationException, IOException, SQLException, NoSuchAlgorithmException;
}
