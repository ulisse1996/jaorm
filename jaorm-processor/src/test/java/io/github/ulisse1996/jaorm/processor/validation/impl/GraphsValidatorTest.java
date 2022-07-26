package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.annotation.Graph;
import io.github.ulisse1996.jaorm.annotation.Relationship;
import io.github.ulisse1996.jaorm.external.support.mock.MockName;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class GraphsValidatorTest {

    @Mock private TypeElement typeElement;
    @Mock private ProcessingEnvironment environment;
    @Mock private VariableElement variableElement;
    @Mock private Graph g1;
    @Mock private Graph g2;

    @InjectMocks private GraphsValidator validator;

    @Test
    void should_throw_exception_for_not_unique_name() {
        try {
            Mockito.when(typeElement.getAnnotationsByType(Graph.class))
                    .thenReturn(new Graph[] {g1, g2});
            Mockito.when(g1.name()).thenReturn("test");
            Mockito.when(g1.nodes()).thenReturn(new String[] {"node"});
            Mockito.when(g2.name()).thenReturn("test");
            Mockito.when(typeElement.getEnclosedElements())
                    .then(invocation -> Collections.singletonList(variableElement));
            Mockito.when(variableElement.getSimpleName())
                    .thenReturn(new MockName("node"));
            Mockito.when(variableElement.getAnnotation(Relationship.class))
                    .thenReturn(Mockito.mock(Relationship.class));
            validator.validate(Collections.singletonList(typeElement));
        } catch (ProcessorException ex) {
            Assertions.assertEquals(
                    "Found not unique name test ! Please choose a different name",
                    ex.getMessage()
            );
        }
    }

    @Test
    void should_throw_exception_for_missing_relationship_annotation() {
        try {
            Mockito.when(typeElement.getAnnotationsByType(Graph.class))
                    .thenReturn(new Graph[] {g1});
            Mockito.when(g1.name()).thenReturn("test");
            Mockito.when(g1.nodes()).thenReturn(new String[] {"node"});
            Mockito.when(typeElement.getEnclosedElements())
                    .then(invocation -> Collections.singletonList(variableElement));
            Mockito.when(variableElement.getSimpleName())
                    .thenReturn(new MockName("node"));
            validator.validate(Collections.singletonList(typeElement));
        } catch (ProcessorException ex) {
            Assertions.assertEquals(
                    "Field with name node is not annotated with @Relationship !",
                    ex.getMessage()
            );
        }
    }

    @Test
    void should_validate_graph() {
        Mockito.when(typeElement.getAnnotationsByType(Graph.class))
                .thenReturn(new Graph[] {g1});
        Mockito.when(g1.name()).thenReturn("test");
        Mockito.when(g1.nodes()).thenReturn(new String[] {"node"});
        Mockito.when(typeElement.getEnclosedElements())
                .then(invocation -> Collections.singletonList(variableElement));
        Mockito.when(variableElement.getSimpleName())
                .thenReturn(new MockName("node"));
        Mockito.when(variableElement.getAnnotation(Relationship.class))
                .thenReturn(Mockito.mock(Relationship.class));
        Assertions.assertDoesNotThrow(() -> validator.validate(Collections.singletonList(typeElement)));
    }
}
