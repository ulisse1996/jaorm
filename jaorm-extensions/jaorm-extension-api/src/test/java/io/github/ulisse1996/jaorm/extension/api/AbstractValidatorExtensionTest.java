package io.github.ulisse1996.jaorm.extension.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class AbstractValidatorExtensionTest {

    @Mock private ProcessingEnvironment environment;

    private final AbstractValidatorExtension extension = new AbstractValidatorExtension() {
        @Override
        public Set<Class<? extends Annotation>> getSupported() {
            return Collections.emptySet();
        }
    };

    @Test
    void should_do_nothing_for_validate() {
        Assertions.assertDoesNotThrow(() -> extension.validate(Collections.emptySet(), environment));
    }

    @Test
    void should_do_nothing_for_validate_sql() {
        Assertions.assertDoesNotThrow(() -> extension.validateSql("SQL", environment));
    }
}
