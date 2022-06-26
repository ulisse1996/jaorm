package io.github.ulisse1996.jaorm.processor.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class ReturnTypeDefinitionTest {

    @Mock private ProcessingEnvironment environment;
    @Mock private TypeMirror mirror;
    @Mock private TypeMirror otherMirror;
    @Mock private Types types;
    @Mock private Elements elements;

    @Test
    void should_return_plain_class() {
        TypeElement element = Mockito.mock(TypeElement.class);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.asElement(mirror))
                .thenReturn(element);
        Mockito.when(mirror.toString())
                .thenReturn("TEST_TYPE");
        ReturnTypeDefinition definition = new ReturnTypeDefinition(environment, mirror);
        Assertions.assertTrue(definition.isSimple());
        Assertions.assertEquals(element, definition.getRealClass());
        assertAllFalls(definition.isOptional(), definition.isCollection(), definition.isStream(), definition.isStreamTableRow(), definition.isTableRow());
    }

    @Test
    void should_return_optional() {
        TypeElement element = Mockito.mock(TypeElement.class);
        TypeMirror mirrorUser = Mockito.mock(TypeMirror.class);
        Mockito.when(mirror.toString())
                .thenReturn("java.util.Optional<io.test.User>");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement("io.test.User"))
                .thenReturn(element);
        Mockito.when(element.asType())
                .thenReturn(mirrorUser);
        Mockito.when(mirrorUser.toString())
                .thenReturn("io.test.User");
        ReturnTypeDefinition definition = new ReturnTypeDefinition(environment, mirror);
        Assertions.assertTrue(definition.isOptional());
        Assertions.assertEquals(element, definition.getRealClass());
        assertAllFalls(definition.isSimple(), definition.isCollection(), definition.isStream(), definition.isStreamTableRow(), definition.isTableRow());
    }

    @Test
    void should_return_optional_table_row() {
        TypeElement element = Mockito.mock(TypeElement.class);
        TypeMirror mirrorTableRow = Mockito.mock(TypeMirror.class);
        Mockito.when(mirror.toString())
                .thenReturn("java.util.Optional<io.github.ulisse1996.jaorm.mapping.TableRow>");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement("io.github.ulisse1996.jaorm.mapping.TableRow"))
                .thenReturn(element);
        Mockito.when(element.asType())
                .thenReturn(mirrorTableRow);
        Mockito.when(mirrorTableRow.toString())
                .thenReturn("io.github.ulisse1996.jaorm.mapping.TableRow");
        ReturnTypeDefinition definition = new ReturnTypeDefinition(environment, mirror);
        Assertions.assertTrue(definition.isOptional());
        Assertions.assertEquals(element, definition.getRealClass());
        Assertions.assertTrue(definition.isTableRow());
        assertAllFalls(definition.isSimple(), definition.isCollection(), definition.isStream(), definition.isStreamTableRow());
    }

    @Test
    void should_return_list() {
        TypeElement element = Mockito.mock(TypeElement.class);
        Mockito.when(mirror.toString())
                .thenReturn("java.util.List<io.test.User>");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement("io.test.User"))
                .thenReturn(element);
        Mockito.when(element.asType())
                .thenReturn(otherMirror);
        Mockito.when(otherMirror.toString()).thenReturn("io.test.User");
        ReturnTypeDefinition definition = new ReturnTypeDefinition(environment, mirror);
        Assertions.assertTrue(definition.isCollection());
        Assertions.assertEquals(element, definition.getRealClass());
        assertAllFalls(definition.isSimple(), definition.isOptional(), definition.isStream(), definition.isStreamTableRow(), definition.isTableRow());
    }

    @Test
    void should_return_stream_of_entity() {
        TypeElement element = Mockito.mock(TypeElement.class);
        TypeMirror afterMirror = Mockito.mock(TypeMirror.class);
        Mockito.when(mirror.toString())
                .thenReturn("java.util.stream.Stream<io.test.User>");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement("io.test.User"))
                .thenReturn(element);
        Mockito.when(element.asType())
                .thenReturn(afterMirror);
        Mockito.when(afterMirror.toString())
                .thenReturn("io.test.User");
        ReturnTypeDefinition definition = new ReturnTypeDefinition(environment, mirror);
        Assertions.assertTrue(definition.isStream());
        Assertions.assertEquals(element, definition.getRealClass());
        assertAllFalls(definition.isSimple(), definition.isOptional(), definition.isCollection(), definition.isStreamTableRow(), definition.isTableRow());
    }

    @Test
    void should_return_stream_of_table_row() {
        TypeElement element = Mockito.mock(TypeElement.class);
        TypeMirror afterMirror = Mockito.mock(TypeMirror.class);
        Mockito.when(mirror.toString())
                .thenReturn("java.util.stream.Stream<io.github.ulisse1996.mapping.TableRow>");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement("io.github.ulisse1996.mapping.TableRow"))
                .thenReturn(element);
        Mockito.when(element.asType())
                .thenReturn(afterMirror);
        Mockito.when(afterMirror.toString())
                .thenReturn("io.github.ulisse1996.jaorm.mapping.TableRow");
        ReturnTypeDefinition definition = new ReturnTypeDefinition(environment, mirror);
        Assertions.assertTrue(definition.isStream());
        Assertions.assertTrue(definition.isStreamTableRow());
        Assertions.assertEquals(element, definition.getRealClass());
        assertAllFalls(definition.isSimple(), definition.isOptional(), definition.isCollection(), definition.isTableRow());
    }

    @Test
    void should_return_table_row() {
        Mockito.when(mirror.toString())
                .thenReturn("io.github.ulisse1996.jaorm.mapping.TableRow");
        ReturnTypeDefinition definition = new ReturnTypeDefinition(environment, mirror);
        Assertions.assertTrue(definition.isTableRow());
        assertAllFalls(definition.isSimple(), definition.isOptional(), definition.isCollection(), definition.isStream(), definition.isStreamTableRow());
    }

    private void assertAllFalls(Boolean... booleans) {
        Arrays.asList(booleans).forEach(Assertions::assertFalse);
    }
}
