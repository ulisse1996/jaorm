package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ReplaceFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);

    @ParameterizedTest
    @MethodSource("getNoReplacement")
    void should_create_replace(Selectable<String> selectable, String expected) {
        Assertions.assertEquals(
                expected,
                AnsiFunctions.replace(selectable, "TERM", "OTHER").apply("MY_TABLE")
        );
    }

    private static Stream<Arguments> getNoReplacement() {
        return Stream.of(
                Arguments.of(COL_1, "REPLACE(MY_TABLE.COL_1, ?, ?)"),
                Arguments.of(AnsiFunctions.upper(COL_1), "REPLACE(UPPER(MY_TABLE.COL_1), ?, ?)"),
                Arguments.of(InlineValue.inline("3_TERM"), "REPLACE(?, ?, ?)")
        );
    }
}