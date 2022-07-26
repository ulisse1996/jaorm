package io.github.ulisse1996.jaorm.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

@ExtendWith(MockitoExtension.class)
class ExtensionLoaderTest {

    @Mock private ProcessingEnvironment environment;
    @Mock private Messager messager;

    @Test
    void should_skip_validation_for_missing_extensions() {
        Mockito.when(environment.getMessager()).thenReturn(messager);
    }
}