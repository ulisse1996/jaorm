package io.github.ulisse1996.jaorm.vendor.db2;

import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class Db2LikeSpecificTest {

    private final Db2LikeSpecific testSubject = new Db2LikeSpecific();

    @ParameterizedTest
    @EnumSource(LikeSpecific.LikeType.class)
    void should_convert_like_type(LikeSpecific.LikeType type) {
        String expected = null;
        switch (type) {

            case FULL:
                expected = " '%' || ? || '%'";
                break;
            case START:
                expected = " '%' || ? ";
                break;
            case END:
                expected = " ? || '%'";
                break;
            default:
                Assertions.fail();
        }
        Assertions.assertEquals(expected, testSubject.convertToLikeSupport(type, false));
    }

    @ParameterizedTest
    @EnumSource(LikeSpecific.LikeType.class)
    void should_convert_like_type_with_case_insensitive(LikeSpecific.LikeType type) {
        String expected = null;
        switch (type) {

            case FULL:
                expected = " '%' || UPPER(?) || '%'";
                break;
            case START:
                expected = " '%' || UPPER(?) ";
                break;
            case END:
                expected = " UPPER(?) || '%'";
                break;
            default:
                Assertions.fail();
        }
        Assertions.assertEquals(expected, testSubject.convertToLikeSupport(type, true));
    }
}