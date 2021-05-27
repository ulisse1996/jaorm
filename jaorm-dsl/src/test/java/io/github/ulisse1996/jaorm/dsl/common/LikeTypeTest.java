package io.github.ulisse1996.jaorm.dsl.common;

import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LikeSpecific;
import io.github.ulisse1996.jaorm.vendor.supports.common.PipeLikeSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class LikeTypeTest {

    @Test
    void should_return_vendor_specific_full() {
        try (MockedStatic<VendorSpecific> vendor = Mockito.mockStatic(VendorSpecific.class)) {
            vendor.when(() -> VendorSpecific.getSpecific(LikeSpecific.class))
                    .thenReturn(PipeLikeSpecific.INSTANCE);
            String result = LikeType.FULL.getValue();
            Assertions.assertEquals("'%' || ? || '%'", result);
        }
    }
}
