package io.github.ulisse1996.jaorm.processor;

import io.github.ulisse1996.jaorm.processor.config.ConfigHolder;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class JaormProcessorTest {

    @Mock private Filer filer;
    @Mock private FileObject object;

    private String name;

    @BeforeEach
    void resetConfig() {
        this.name = System.getProperty("os.name");
        ConfigHolder.destroy();
    }

    @AfterEach
    void reset() {
        System.setProperty("os.name", this.name);
    }

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
    void should_return_true_for_correct_process() throws IOException {
        try (MockedStatic<Generator> genMock = Mockito.mockStatic(Generator.class);
            MockedStatic<Validator> valMock = Mockito.mockStatic(Validator.class)) {
            genMock.when(() -> Generator.forType(Mockito.any(), Mockito.any()))
                    .thenReturn(EMPTY_GENERATOR);
            valMock.when(() -> Validator.forType(Mockito.any(), Mockito.any()))
                    .thenReturn(EMPTY_VALIDATOR);
            JaormProcessor processor = new JaormProcessor() {{
                processingEnv = Mockito.mock(ProcessingEnvironment.class);
                Mockito.when(processingEnv.getOptions())
                        .thenReturn(Collections.emptyMap());
                Mockito.when(processingEnv.getFiler())
                        .thenReturn(filer);
                Mockito.when(filer.createResource(Mockito.any(), Mockito.anyString(), Mockito.any()))
                        .thenReturn(object);
                Mockito.when(object.toUri())
                        .thenReturn(URI.create("mem://hello"));
            }};
            boolean result = processor.process(Collections.emptySet(), Mockito.mock(RoundEnvironment.class));
            Assertions.assertTrue(result);
        }
    }

    @Test
    void should_init_services() throws IOException {
        Path file = Files.createTempFile("file", ".tmp");
        try (MockedStatic<Generator> genMock = Mockito.mockStatic(Generator.class);
             MockedStatic<Validator> valMock = Mockito.mockStatic(Validator.class)) {
            genMock.when(() -> Generator.forType(Mockito.any(), Mockito.any()))
                    .then(invocation -> {
                        Assertions.assertEquals(file.getParent(), ConfigHolder.getServices());
                        return EMPTY_GENERATOR;
                    });
            valMock.when(() -> Validator.forType(Mockito.any(), Mockito.any()))
                    .thenReturn(EMPTY_VALIDATOR);
            JaormProcessor processor = new JaormProcessor() {{
                processingEnv = Mockito.mock(ProcessingEnvironment.class);
                Mockito.when(processingEnv.getOptions())
                        .thenReturn(Collections.emptyMap());
                Mockito.when(processingEnv.getFiler())
                        .thenReturn(filer);
                Mockito.when(filer.createResource(Mockito.any(), Mockito.anyString(), Mockito.any()))
                        .thenReturn(object);
                Mockito.when(object.toUri())
                        .thenReturn(file.toUri());
            }};
            boolean result = processor.process(Collections.emptySet(), Mockito.mock(RoundEnvironment.class));
            Assertions.assertTrue(result);
            Assertions.assertFalse(Files.exists(file));
        }
    }

    @Test
    void should_init_services_with_windows_and_bad_path() throws IOException {
        Path file = Files.createTempFile("file", ".tmp");
        try (MockedStatic<Generator> genMock = Mockito.mockStatic(Generator.class);
             MockedStatic<Validator> valMock = Mockito.mockStatic(Validator.class)) {
            genMock.when(() -> Generator.forType(Mockito.any(), Mockito.any()))
                    .then(invocation -> {
                        Assertions.assertEquals(file.getParent(), ConfigHolder.getServices());
                        return EMPTY_GENERATOR;
                    });
            valMock.when(() -> Validator.forType(Mockito.any(), Mockito.any()))
                    .thenReturn(EMPTY_VALIDATOR);
            String name = file.getParent().toString();
            FilerException exception = new FilerException(String.format("Attempt to reopen a file for path /%s", name));
            System.setProperty("os.name", "Windows");
            JaormProcessor processor = new JaormProcessor() {{
                processingEnv = Mockito.mock(ProcessingEnvironment.class);
                Mockito.when(processingEnv.getOptions())
                        .thenReturn(Collections.emptyMap());
                Mockito.when(processingEnv.getFiler())
                        .thenReturn(filer);
                Mockito.when(filer.createResource(Mockito.any(), Mockito.anyString(), Mockito.any()))
                        .thenThrow(exception);
            }};
            boolean result = processor.process(Collections.emptySet(), Mockito.mock(RoundEnvironment.class));
            Assertions.assertTrue(result);
            Assertions.assertTrue(Files.exists(file));
            Files.deleteIfExists(file);
        }
    }
}
