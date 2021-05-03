package io.github.ulisse1996.jaorm.processor;

import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.util.Collections;
import java.util.List;

class JaormProcessorTest {

    private static final Generator EMPTY_GENERATOR = new Generator(Mockito.mock(ProcessingEnvironment.class)) {
        @Override
        public void generate(RoundEnvironment roundEnvironment) {
            // Nothing
        }
    };
    private static final Validator EMPTY_VALIDATOR = new Validator(Mockito.mock(ProcessingEnvironment.class)) {
        @Override
        public void validate(List<? extends Element> annotated) {
            // Nothing
        }
    };

    @Test
    void should_return_true_for_correct_process() {
        try (MockedStatic<Generator> genMock = Mockito.mockStatic(Generator.class);
            MockedStatic<Validator> valMock = Mockito.mockStatic(Validator.class)) {
            genMock.when(() -> Generator.forType(Mockito.any(), Mockito.any()))
                    .thenReturn(EMPTY_GENERATOR);
            valMock.when(() -> Validator.forType(Mockito.any(), Mockito.any()))
                    .thenReturn(EMPTY_VALIDATOR);
            boolean result = new JaormProcessor().process(Collections.emptySet(), Mockito.mock(RoundEnvironment.class));
            Assertions.assertTrue(result);
        }
    }
}
