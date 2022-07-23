package io.github.ulisse1996.jaorm.extension.ansi;

import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.extension.api.exception.ProcessorValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Collections;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class QueryAnsiValidatorTest {

    @Mock private ProcessingEnvironment processingEnvironment;

    @ParameterizedTest
    @MethodSource("getValidSql")
    void should_validate_sql(String sql) {
        QueryAnsiValidator validator = new QueryAnsiValidator();
        validator.validateSql(sql, processingEnvironment);
    }

    @ParameterizedTest
    @MethodSource("getInvalidSql")
    void should_throw_exception_for_unsupported_sql(String sql) {
        QueryAnsiValidator validator = new QueryAnsiValidator();
        Assertions.assertThrows(ProcessorValidationException.class,
                () -> validator.validateSql(sql, processingEnvironment));
    }

    @Test
    void should_return_only_query_annotation() {
        Assertions.assertEquals(
                Collections.singleton(Query.class),
                new QueryAnsiValidator().getSupported()
        );
    }

    @Test
    void should_throw_exception_for_parsing_error() {
        Assertions.assertThrows(
                ProcessorValidationException.class,
                () -> new QueryAnsiValidator().validateSql("SELECT ", processingEnvironment)
        );
    }

    public static Stream<Arguments> getInvalidSql() {
        return Stream.of(
                Arguments.of("SELECT NVL(COL, 'null') FROM MY_TABLE"),
                Arguments.of("SELECT * FROM MY_TABLE WHERE COL_2 = NVL(?, 2)"),
                Arguments.of("SELECT * FROM MY_TABLE WHERE COL_2 > NVL(?, 2)"),
                Arguments.of("SELECT * FROM MY_TABLE WHERE COL_2 < NVL(?, 2)"),
                Arguments.of("SELECT * FROM MY_TABLE WHERE COL_2 >= NVL(?, 2)"),
                Arguments.of("SELECT * FROM MY_TABLE WHERE COL_2 <= NVL(?, 2)"),
                Arguments.of("SELECT * FROM MY_TABLE WHERE COL_2 IN (NVL(?, 1))"),
                Arguments.of("SELECT * FROM MY_TABLE WHERE COL_2 <= NVL(?, 3) AND COL_3 >= NVL(?, 4)"),
                Arguments.of("SELECT CASE WHEN COL_2 = ? THEN NVL(COL_3, COL_2) ELSE 3 END FROM MY_TABLE WHERE COL_1 = ?"),
                Arguments.of("SELECT CASE WHEN COL_2 = NVL(COL3, 2) THEN 3 ELSE 3 END FROM MY_TABLE WHERE COL_1 = ?"),
                Arguments.of("SELECT CASE WHEN COL_2 = 4 THEN 3 ELSE NVL(COL_3, 6) END FROM MY_TABLE WHERE COL_1 = ?"),
                Arguments.of("UPDATE MY_TABLE SET COL_1 = NVL(?, 'THIS') WHERE 1 = 1"),
                Arguments.of("UPDATE MY_TABLE SET COL_1 = ? WHERE COL_2 = NVL(?, 'THIS')"),
                Arguments.of("DELETE FROM MY_TABLE WHERE COL_1 IN (SELECT NVL(?, '3') FROM MY_TABLE2 WHERE 1 = 1)")
        );
    }

    public static Stream<Arguments> getValidSql() {
        return Stream.of(
                Arguments.of("SELECT * FROM MY_TABLE"),
                Arguments.of("SELECT * FROM MY_TALBLE WHERE COL1 = ?")
        );
    }
}
