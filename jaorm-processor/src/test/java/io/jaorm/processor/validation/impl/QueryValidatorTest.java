package io.jaorm.processor.validation.impl;

import io.jaorm.annotation.Query;
import io.jaorm.processor.CustomName;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.util.ReturnTypeDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.stream.Stream;

class QueryValidatorTest {

    private final QueryValidator testSubject = new QueryValidator(Mockito.mock(ProcessingEnvironment.class));

    @Test
    void should_throw_exception_for_unsupported_sql() {
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("NOT VALID");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        try {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Can't find query strategy for method NAME"));
        }
    }

    @Test
    void should_throw_exception_for_parameters_mismatch() {
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("SELECT * FROM TABLE WHERE COL = ?");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        Mockito.when(method.getParameters())
                .thenReturn(Collections.emptyList());
        try {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Mismatch between parameters and query parameters for method NAME"));
        }
    }

    @Test
    void should_throw_exception_for_select_with_void() {
        TypeMirror voidReturn = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(voidReturn.getKind())
                .thenReturn(TypeKind.VOID);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("SELECT * FROM TABLE WHERE COL = ?");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        Mockito.when(method.getParameters())
                .then(invocation -> Collections.singletonList(Mockito.mock(VariableElement.class)));
        Mockito.when(method.getReturnType())
                .thenReturn(voidReturn);
        try {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Can't use Select statement with a void method"));
        }
    }

    @Test
    void should_throw_exception_for_delete_without_void() {
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(returnType.getKind())
                .thenReturn(TypeKind.ARRAY);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("DELETE TABLE WHERE COL = ?");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        Mockito.when(method.getParameters())
                .then(invocation -> Collections.nCopies(1, Mockito.mock(VariableElement.class)));
        Mockito.when(method.getReturnType())
                .thenReturn(returnType);
        try {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Can't use Delete or Update statement with a non-void method"));
        }
    }

    @Test
    void should_throw_exception_for_update_without_void() {
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(returnType.getKind())
                .thenReturn(TypeKind.ARRAY);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("UPDATE TABLE SET COL1 = ? WHERE COL = ?");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        Mockito.when(method.getParameters())
                .then(invocation -> Collections.nCopies(2, Mockito.mock(VariableElement.class)));
        Mockito.when(method.getReturnType())
                .thenReturn(returnType);
        try {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Can't use Delete or Update statement with a non-void method"));
        }
    }

    @Test
    void should_pass_for_update_with_void() {
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(returnType.getKind())
                .thenReturn(TypeKind.VOID);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("UPDATE TABLE SET COL1 = ? WHERE COL = ?");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        Mockito.when(method.getParameters())
                .then(invocation -> Collections.nCopies(2, Mockito.mock(VariableElement.class)));
        Mockito.when(method.getReturnType())
                .thenReturn(returnType);
        try {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_throw_exception_for_missing_sql_query_case() {
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(returnType.getKind())
                .thenReturn(TypeKind.VOID);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("MERGE TABLE SET COL1 = ? WHERE COL = ?");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        Mockito.when(method.getParameters())
                .then(invocation -> Collections.nCopies(2, Mockito.mock(VariableElement.class)));
        Mockito.when(method.getReturnType())
                .thenReturn(returnType);
        try {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Operation not supported for sql"));
        }
    }

    @Test
    void should_find_correct_return_type_for_select() {
        TypeMirror voidReturn = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(voidReturn.getKind())
                .thenReturn(TypeKind.NONE);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("SELECT * FROM TABLE WHERE COL = ?");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        Mockito.when(method.getParameters())
                .then(invocation -> Collections.singletonList(Mockito.mock(VariableElement.class)));
        Mockito.when(method.getReturnType())
                .thenReturn(voidReturn);
        try (MockedConstruction<ReturnTypeDefinition> mk =
                     Mockito.mockConstruction(ReturnTypeDefinition.class)) {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.fail(ex);
        }
    }

    @ParameterizedTest
    @MethodSource("getCases")
    void should_throw_exception_for_missing_entity(boolean optional, boolean collection , boolean stream) {
        TypeMirror voidReturn = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(voidReturn.getKind())
                .thenReturn(TypeKind.NONE);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("SELECT * FROM TABLE WHERE COL = ?");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        Mockito.when(method.getParameters())
                .then(invocation -> Collections.singletonList(Mockito.mock(VariableElement.class)));
        Mockito.when(method.getReturnType())
                .thenReturn(voidReturn);
        try (MockedConstruction<ReturnTypeDefinition> mk =
                     Mockito.mockConstruction(ReturnTypeDefinition.class, (mock, context) -> {
                         if (optional) {
                             Mockito.when(mock.isOptional())
                                     .thenReturn(true);
                         } else if (stream) {
                             Mockito.when(mock.isStream())
                                     .thenReturn(true);
                         } else {
                             Mockito.when(mock.isCollection())
                                     .thenReturn(true);
                         }
                     })) {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("is not a valid return type for Query method"));
        }
    }

    private static Stream<Arguments> getCases() {
        return Stream.of(
                Arguments.of(true, false, false),
                Arguments.of(false, true, false),
                Arguments.of(false, false, true)
        );
    }
}