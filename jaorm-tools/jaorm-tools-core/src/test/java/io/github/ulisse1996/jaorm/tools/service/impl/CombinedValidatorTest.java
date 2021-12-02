package io.github.ulisse1996.jaorm.tools.service.impl;

import io.github.ulisse1996.jaorm.tools.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.tools.exception.QueryValidationException;
import io.github.ulisse1996.jaorm.tools.service.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

@ExtendWith(MockitoExtension.class)
class CombinedValidatorTest {

    @Mock private Validator val1;
    @Mock private Validator val2;

    @Test
    void should_call_entity_validation_for_each_validator() throws EntityValidationException, SQLException, IOException, NoSuchAlgorithmException {
        CombinedValidator validator = new CombinedValidator(val1, val2);
        validator.validateEntities();
        Mockito.verify(val1).validateEntities();
        Mockito.verify(val2).validateEntities();
    }

    @Test
    void should_call_sql_validation_for_each_validator() throws IOException, NoSuchAlgorithmException, QueryValidationException {
        CombinedValidator validator = new CombinedValidator(val1, val2);
        validator.validateQueries();
        Mockito.verify(val1).validateQueries();
        Mockito.verify(val2).validateQueries();
    }
}
