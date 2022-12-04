package io.github.ulisse1996.jaorm.processor;

import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.ServiceLoader;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ExtensionLoaderTest {

    @Mock private ProcessingEnvironment environment;
    @Mock private Messager messager;
    @Mock private ServiceLoader<?> serviceLoader;

    @Test
    void should_skip_validation_for_missing_extensions() {
        ClassLoader loader = new CustomLoader(true);
        ExtensionLoader extensionLoader = ExtensionLoader.getInstance(loader);
        Mockito.when(environment.getMessager()).thenReturn(messager);

        extensionLoader.loadValidationExtensions(environment);

        Mockito.verify(messager)
                .printMessage(Mockito.eq(Diagnostic.Kind.NOTE), Mockito.anyString());
    }

    @SuppressWarnings({"rawtypes", "RedundantOperationOnEmptyContainer"})
    @Test
    void should_return_empty_classes() {
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            ClassLoader loader = new CustomLoader(false);
            ExtensionLoader extensionLoader = ExtensionLoader.getInstance(loader);
            mk.when(() -> ServiceLoader.load(Mockito.any(), Mockito.any(ClassLoader.class)))
                    .thenReturn(serviceLoader);
            Mockito.when(serviceLoader.spliterator()).then(i -> Collections.emptyList().spliterator());

            Assertions.assertEquals(
                    Collections.emptyList(),
                    extensionLoader.loadValidationExtensions(environment)
            );
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void should_throw_exception_for_service_load() {
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            ClassLoader loader = new CustomLoader(false);
            ExtensionLoader extensionLoader = ExtensionLoader.getInstance(loader);
            mk.when(() -> ServiceLoader.load(Mockito.any(), Mockito.any(ClassLoader.class)))
                    .thenThrow(IllegalArgumentException.class);

            Assertions.assertThrows(ProcessorException.class, () -> extensionLoader.loadValidationExtensions(environment));
        }
    }

    @Test
    void should_call_validate_method() {
        MyObject mock = Mockito.mock(MyObject.class);
        ExtensionLoader.ExtensionManager manager = new ExtensionLoader.ExtensionManager(mock);
        manager.executeValidation(Collections.emptySet(), environment);

        Mockito.verify(mock)
                .validate(Mockito.any(), Mockito.any());
    }

    @Test
    void should_call_validate_sql_method() {
        MyObject mock = Mockito.mock(MyObject.class);
        ExtensionLoader.ExtensionManager manager = new ExtensionLoader.ExtensionManager(mock);
        manager.executeValidation("", environment);

        Mockito.verify(mock)
                .validateSql(Mockito.anyString(), Mockito.any());
    }

    @Test
    void should_call_get_supported() {
        MyObject mock = Mockito.mock(MyObject.class);
        ExtensionLoader.ExtensionManager manager = new ExtensionLoader.ExtensionManager(mock);
        manager.getSupported();

        Mockito.verify(mock)
                .getSupported();
    }

    @Test
    void should_throw_exception_for_validate_method() {
        MyObject mock = Mockito.mock(MyObject.class);
        ExtensionLoader.ExtensionManager manager = new ExtensionLoader.ExtensionManager(mock);

        Mockito.doThrow(IllegalArgumentException.class)
                .when(mock).validate(Mockito.anySet(), Mockito.any());

        Assertions.assertThrows( //NOSONAR
                ProcessorException.class,
                () -> manager.executeValidation(Collections.emptySet(), environment)
        );
    }

    @Test
    void should_throw_exception_for_validate_sql_method() {
        MyObject mock = Mockito.mock(MyObject.class);
        ExtensionLoader.ExtensionManager manager = new ExtensionLoader.ExtensionManager(mock);

        Mockito.doThrow(IllegalArgumentException.class)
                .when(mock).validateSql(Mockito.any(), Mockito.any());

        Assertions.assertThrows( // NOSONAR
                ProcessorException.class,
                () -> manager.executeValidation("", environment)
        );
    }

    @Test
    void should_throw_exception_for_get_supported() {
        MyObject mock = Mockito.mock(MyObject.class);
        ExtensionLoader.ExtensionManager manager = new ExtensionLoader.ExtensionManager(mock);

        Mockito.when(mock.getSupported()).thenThrow(IllegalArgumentException.class);

        Assertions.assertThrows( //NOSONAR
                ProcessorException.class,
                manager::getSupported
        );
    }

    @Test
    void should_call_get_supported_with_cache() {
        MyObject mock = Mockito.mock(MyObject.class);
        ExtensionLoader.ExtensionManager manager = new ExtensionLoader.ExtensionManager(mock);
        manager.getSupported();
        manager.getSupported();

        Mockito.verify(mock, Mockito.times(2))
                .getSupported();
    }

    private static class MyObject {

        @SuppressWarnings("UnusedReturnValue")
        Set<Class<? extends Annotation>> getSupported() {
            return Collections.emptySet();
        }

        void validate(Set<Element> elements, ProcessingEnvironment processingEnvironment) {}
        void validateSql(String sql, ProcessingEnvironment processingEnvironment) {}
    }

    private static class CustomLoader extends ClassLoader {

        private final boolean isThrow;

        private CustomLoader(boolean isThrow) {
            this.isThrow = isThrow;
        }

        @Override
        public Class<?> loadClass(String className) throws ClassNotFoundException {
            if (isThrow) {
                throw new ClassNotFoundException();
            }
            return Object.class;
        }
    }
}