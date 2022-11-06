package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.InlineValue;
import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LengthSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class LengthFunctionTest {

    private static final SqlColumn<Object, String> COL_1 = SqlColumn.simple("COL_1", String.class);

    @Mock private VendorFunction<String> delegate;
    @Mock private LengthSpecific specific;

    @Test
    void should_return_false_for_string_function() {
        Assertions.assertFalse(AnsiFunctions.length(COL_1).isString());
    }

    @ParameterizedTest
    @MethodSource("getTests")
    void should_create_length(Selectable<String> selectable, String expected) {
        try (MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            mkVendor.when(() -> VendorSpecific.getSpecific(LengthSpecific.class, LengthSpecific.NO_OP))
                    .thenReturn(LengthSpecific.NO_OP);
            Assertions.assertEquals(
                    expected,
                    AnsiFunctions.length(selectable).apply("MY_TABLE")
            );
        }
    }

    @Test
    void should_use_delegate_length_fn() {
        try (MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class)) {
            mkVendor.when(() -> VendorSpecific.getSpecific(LengthSpecific.class, LengthSpecific.NO_OP))
                    .thenReturn(specific);
            Mockito.when(specific.apply(Mockito.any()))
                    .thenReturn(delegate);

            AnsiFunctions.length(COL_1).apply("");

            Mockito.verify(delegate)
                    .apply("");
        }
    }

    @Test
    void should_get_empty_params_for_column() {
        Assertions.assertEquals(
                Collections.emptyList(),
                AnsiFunctions.length(COL_1).getParams()
        );
    }

    @Test
    void should_get_param_from_inline() {
        Assertions.assertEquals(
                Collections.singletonList("323"),
                AnsiFunctions.length(InlineValue.inline("323")).getParams()
        );
    }

    private static Stream<Arguments> getTests() {
        return Stream.of(
                Arguments.of(COL_1, "LENGTH(MY_TABLE.COL_1)"),
                Arguments.of(AnsiFunctions.upper(COL_1), "LENGTH(UPPER(MY_TABLE.COL_1))"),
                Arguments.of(InlineValue.inline("31234"), "LENGTH(?)")
        );
    }
}