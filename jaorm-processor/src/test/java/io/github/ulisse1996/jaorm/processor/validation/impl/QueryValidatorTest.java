package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.external.support.mock.MockName;
import io.github.ulisse1996.jaorm.processor.CustomName;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.util.ReturnTypeDefinition;
import io.github.ulisse1996.jaorm.specialization.SingleKeyDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class QueryValidatorTest {

    @Mock private ProcessingEnvironment environment;
    @Mock private Messager messager;
    @InjectMocks private QueryValidator testSubject;

    @Test
    void should_throw_exception_for_unsupported_sql() {
        Mockito.when(environment.getMessager()).thenReturn(messager);
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
        Mockito.when(environment.getMessager()).thenReturn(messager);
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
        Mockito.when(environment.getMessager()).thenReturn(messager);
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
        Mockito.when(environment.getMessager()).thenReturn(messager);
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
        Mockito.when(environment.getMessager()).thenReturn(messager);
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
        Mockito.when(environment.getMessager()).thenReturn(messager);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(returnType.getKind())
                .thenReturn(TypeKind.VOID);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("UPDATE MY_TABLE SET COL1 = ? WHERE COL = ?");
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
        Mockito.when(environment.getMessager()).thenReturn(messager);
        TypeMirror returnType = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("MERGE TABLE SET COL1 = ? WHERE COL = ?");
        Mockito.when(method.getSimpleName())
                .thenReturn(new CustomName("NAME"));
        Mockito.when(method.getParameters())
                .then(invocation -> Collections.nCopies(2, Mockito.mock(VariableElement.class)));
        try {
            testSubject.validate(Collections.singletonList(method));
        } catch (ProcessorException ex) {
            Assertions.assertTrue(ex.getMessage().contains("Operation not supported for sql"));
        }
    }

    @Test
    void should_find_correct_return_type_for_select() {
        Mockito.when(environment.getMessager()).thenReturn(messager);
        TypeMirror voidReturn = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(voidReturn.getKind())
                .thenReturn(TypeKind.NONE);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("SELECT * FROM MY_TABLE WHERE COL = ?");
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

    @Test
    void should_throw_exception_for_mismatch_between_keys_and_dao() {
        Mockito.when(environment.getMessager()).thenReturn(messager);
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            VariableElement v1 = Mockito.mock(VariableElement.class);
            VariableElement v2 = Mockito.mock(VariableElement.class);
            TypeElement typeElement = Mockito.mock(TypeElement.class);
            Elements elements = Mockito.mock(Elements.class);
            Mockito.when(typeElement.getSimpleName()).thenReturn(new MockName("name"));
            Mockito.when(typeElement.getAnnotation(Dao.class))
                    .thenReturn(Mockito.mock(Dao.class));
            Mockito.when(typeElement.getAnnotation(Query.class)).thenReturn(null);
            mk.when(() -> ProcessorUtils.getAllValidElements(environment, typeElement))
                    .thenReturn(Arrays.asList(v1, v2));
            mk.when(() -> ProcessorUtils.isSubType(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocation -> {
                        Class<?> klass = invocation.getArgument(2);
                        return klass.equals(SingleKeyDao.class);
                    });
            mk.when(() -> ProcessorUtils.getBaseDaoGeneric(Mockito.any(), Mockito.any()))
                    .thenReturn("test");
            Mockito.when(environment.getElementUtils()).thenReturn(elements);
            Mockito.when(elements.getTypeElement("test")).thenReturn(typeElement);

            Mockito.when(v1.getAnnotation(Id.class)).thenReturn(Mockito.mock(Id.class));
            Mockito.when(v2.getAnnotation(Id.class)).thenReturn(Mockito.mock(Id.class));

            try {
                testSubject.validate(Collections.singletonList(typeElement));
            } catch (ProcessorException exception) {
                Assertions.assertEquals(
                        "Error on name ! Required 1 @Id but found 2 in Entity",
                        exception.getMessage()
                );
                return;
            }

            Assertions.fail("Should throw exception !");
        }
    }

    @ParameterizedTest
    @MethodSource("getCases")
    void should_throw_exception_for_missing_entity(boolean optional, boolean collection , boolean stream) {
        Mockito.when(environment.getMessager()).thenReturn(messager);
        TypeMirror voidReturn = Mockito.mock(TypeMirror.class);
        Query query = Mockito.mock(Query.class);
        ExecutableElement method = Mockito.mock(ExecutableElement.class);
        Mockito.when(voidReturn.getKind())
                .thenReturn(TypeKind.NONE);
        Mockito.when(method.getAnnotation(Query.class))
                .thenReturn(query);
        Mockito.when(query.sql())
                .thenReturn("SELECT * FROM MY_TABLE WHERE COL = ?");
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
