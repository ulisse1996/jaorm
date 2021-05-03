package io.github.ulisse1996.jaorm.processor.strategy;

import com.squareup.javapoet.ClassName;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.annotation.Param;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class QueryStrategyTest {

    @Test
    void should_validate_wildcard_strategy() {
        QueryStrategy strategy = QueryStrategy.WILDCARD;
        String query = "SELECT * FROM TABLE WHERE X = ? AND Y = ?";
        ExecutableElement method = mockedMethod(2, "n", "y");
        String expectedQuery = "SELECT * FROM TABLE WHERE X = ? AND Y = ?";
        int expectedParams = 2;
        boolean valid = strategy.isValid(query, false);
        int paramNumber = strategy.getParamNumber(query);
        String actual = strategy.replaceQuery(query);
        Assertions.assertDoesNotThrow(() -> strategy.extract(Mockito.mock(ProcessingEnvironment.class), query, method));
        Assertions.assertTrue(valid);
        Assertions.assertEquals(expectedParams, paramNumber);
        Assertions.assertEquals(expectedQuery, actual);
    }

    @Test
    void should_validate_named_strategy() {
        QueryStrategy strategy = QueryStrategy.NAMED;
        String query = "SELECT * FROM TABLE WHERE X = :X AND Y = :Y AND Z = :Y";
        ExecutableElement method = mockedMethod(2, "X","Y");
        String expectedQuery = "SELECT * FROM TABLE WHERE X = ? AND Y = ? AND Z = ?";
        int expectedParams = 2;
        boolean valid = strategy.isValid(query, false);
        int paramNumber = strategy.getParamNumber(query);
        String actual = strategy.replaceQuery(query);
        int words = strategy.extract(Mockito.mock(ProcessingEnvironment.class), query, method).size();
        Assertions.assertTrue(valid);
        Assertions.assertEquals(expectedParams, paramNumber);
        Assertions.assertEquals(expectedQuery, actual);
        Assertions.assertEquals(3, words);
    }

    @Test
    void should_validate_ordered_wildcard_strategy() {
        QueryStrategy strategy = QueryStrategy.ORDERED_WILDCARD;
        String query = "SELECT * FROM TABLE WHERE XA = ?1 AND AM = ?2 AND ZA = ?3";
        ExecutableElement method = mockedMethod(3,"X","Y", "Z");
        String expectedQuery = "SELECT * FROM TABLE WHERE XA = ? AND AM = ? AND ZA = ?";
        int expectedParams = 3;
        boolean valid = strategy.isValid(query, false);
        int paramNumber = strategy.getParamNumber(query);
        String actual = strategy.replaceQuery(query);
        Assertions.assertDoesNotThrow(() -> strategy.extract(Mockito.mock(ProcessingEnvironment.class), query, method));
        Assertions.assertTrue(valid);
        Assertions.assertEquals(expectedParams, paramNumber);
        Assertions.assertEquals(expectedQuery, actual);
    }

    @Test
    void should_validate_at_named_strategy() {
        QueryStrategy strategy = QueryStrategy.AT_NAMED;
        String query = "SELECT * FROM TABLE WHERE X = @X AND Y = @Y AND Z = @Y";
        ExecutableElement method = mockedMethod(2, "X","Y");
        String expectedQuery = "SELECT * FROM TABLE WHERE X = ? AND Y = ? AND Z = ?";
        int expectedParams = 2;
        boolean valid = strategy.isValid(query, false);
        int paramNumber = strategy.getParamNumber(query);
        String actual = strategy.replaceQuery(query);
        int words = strategy.extract(Mockito.mock(ProcessingEnvironment.class), query, method).size();
        Assertions.assertTrue(valid);
        Assertions.assertEquals(expectedParams, paramNumber);
        Assertions.assertEquals(expectedQuery, actual);
        Assertions.assertEquals(3, words);
    }

    @Test
    void should_validate_query_with_no_args() {
        QueryStrategy strategy = QueryStrategy.NO_ARGS;
        String query = "SELECT * FROM TABLE";
        int expectedParams = 0;
        boolean valid = strategy.isValid(query, true);
        ExecutableElement method = mockedMethod(0);
        int paramNumber = strategy.getParamNumber(query);
        String actual = strategy.replaceQuery(query);
        int words = strategy.extract(Mockito.mock(ProcessingEnvironment.class), query, method).size();
        Assertions.assertTrue(valid);
        Assertions.assertEquals(expectedParams, paramNumber);
        Assertions.assertEquals(query, actual);
        Assertions.assertEquals(0, words);
    }

    @Test
    void should_not_find_parameter_with_name() {
        QueryStrategy strategy = QueryStrategy.AT_NAMED;
        String query = "SELECT * FROM TABLE WHERE X = @X AND Y = @Y AND Z = @Y";
        boolean valid = strategy.isValid(query, false);
        ExecutableElement method = mockedMethod(2, "X","Z");
        Assertions.assertTrue(valid);
        Assertions.assertThrows(ProcessorException.class, () -> strategy.extract(Mockito.mock(ProcessingEnvironment.class), query, method));
    }

    private static ExecutableElement mockedMethod(int paramNumber, String... names) {
        ExecutableElement mock = Mockito.mock(ExecutableElement.class);
        List<VariableElement> params = IntStream.range(0, paramNumber)
                .mapToObj(i -> {
                    if (i % 2 == 0) {
                        return mockVariable(true, names[i]);
                    } else {
                        return mockVariable(false, names[i]);
                    }
                }).collect(Collectors.toList());
        Mockito.when(mock.getParameters())
                .then(invocation -> params);
        return mock;
    }

    private static VariableElement mockVariable(boolean withParam, String name) {
        VariableElement mock = Mockito.mock(VariableElement.class);
        if (withParam) {
            Mockito.when(mock.getAnnotation(Param.class))
                    .thenReturn(new Param(){
                        @Override
                        public Class<? extends Annotation> annotationType() {
                            return Param.class;
                        }

                        @Override
                        public String name() {
                            return name;
                        }
                    });
        } else {
            Mockito.when(mock.getSimpleName())
                    .thenReturn(getName(name));
        }
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        Mockito.when(mock.asType())
                .thenReturn(mirror);
        Mockito.when(mirror.accept(Mockito.any(), Mockito.any()))
                .thenReturn(ClassName.get(String.class));
        return mock;
    }

    private static Name getName(String name) {
        return new Name() {
            @Override
            public boolean contentEquals(CharSequence cs) {
                return name.contentEquals(cs);
            }

            @Override
            public int length() {
                return name.length();
            }

            @Override
            public char charAt(int index) {
                return name.charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return name.subSequence(start, end);
            }

            @Override
            @Nonnull
            public String toString() {
                return name;
            }
        };
    }
}
