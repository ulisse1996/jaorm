package io.github.ulisse1996.jaorm.vendor.util;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ArgumentsUtilsTest {

    @ParameterizedTest
    @MethodSource("getFn")
    void should_throw_exception_for_invalid_number_fn(Selectable<?> selectable) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> AnsiFunctions.max(selectable)
        );
    }

    public static Stream<Arguments> getFn() {
        return Stream.of(
                Arguments.of(InlineValue.inline("")),
                Arguments.of(SqlColumn.simple("NAME", String.class)),
                Arguments.of(AnsiFunctions.coalesce(InlineValue.inline("")))
        );
    }
}