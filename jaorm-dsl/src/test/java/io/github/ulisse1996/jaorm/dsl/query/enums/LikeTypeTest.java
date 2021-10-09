package io.github.ulisse1996.jaorm.dsl.query.enums;

import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class LikeTypeTest {

    @Test
    void should_return_vendor_specific_full() {
        LikeSpecific likeSpecific = (type, caseInsensitiveLike) -> {
            switch (type) {
                case FULL:
                    return "'%' || ? || '%'";
                case START:
                    return "'%' || ? ";
                case END:
                    return " ? || '%'";
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
        };
        try (MockedStatic<VendorSpecific> vendor = Mockito.mockStatic(VendorSpecific.class)) {
            vendor.when(() -> VendorSpecific.getSpecific(LikeSpecific.class))
                    .thenReturn(likeSpecific);
            String result = LikeType.FULL.getValue(false);
            Assertions.assertEquals("'%' || ? || '%'", result);
        }
    }
}
