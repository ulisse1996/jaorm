package io.github.ulisse1996.jaorm.vendor.sqlserver;

import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SqlServerLikeSpecificTest {

    private final SqlServerLikeSpecific testSubject = new SqlServerLikeSpecific();

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
        Assertions.assertEquals(expected, testSubject.convertToLikeSupport(type));
    }
}
