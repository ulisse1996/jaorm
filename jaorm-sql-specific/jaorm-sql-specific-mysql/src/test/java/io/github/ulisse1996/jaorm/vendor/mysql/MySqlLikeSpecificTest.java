package io.github.ulisse1996.jaorm.vendor.mysql;

import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MySqlLikeSpecificTest {

    private final MySqlLikeSpecific testSubject = new MySqlLikeSpecific();

    @ParameterizedTest
    @EnumSource(LikeSpecific.LikeType.class)
    void should_convert_like_type(LikeSpecific.LikeType type) {
        String expected = null;
        switch (type) {

            case FULL:
                expected = " CONCAT('%',?,'%')";
                break;
            case START:
                expected = " CONCAT('%',?)";
                break;
            case END:
                expected = " CONCAT(?,'%')";
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
                expected = " CONCAT('%',UPPER(?),'%')";
                break;
            case START:
                expected = " CONCAT('%',UPPER(?))";
                break;
            case END:
                expected = " CONCAT(UPPER(?),'%')";
                break;
            default:
                Assertions.fail();
        }
        Assertions.assertEquals(expected, testSubject.convertToLikeSupport(type, true));
    }
}
