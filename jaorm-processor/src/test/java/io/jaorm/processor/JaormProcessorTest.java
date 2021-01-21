package io.jaorm.processor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.NoInteractions;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class JaormProcessorTest {

    @InjectMocks private JaormProcessor processor;
    @Mock private ProcessingEnvironment environment;

    @Test
    void should_create_delegates() {
        RoundEnvironment roundEnvironment = Mockito.mock(RoundEnvironment.class);
        boolean actual = processor.process(Collections.emptySet(), roundEnvironment);
        Assertions.assertTrue(actual);
        Mockito.verify(environment, new NoInteractions()).getFiler();
    }
}