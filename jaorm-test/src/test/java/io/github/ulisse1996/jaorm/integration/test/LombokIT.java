package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.integration.test.lombok.LombokEntity;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LombokIT {

    @Test
    void should_check_generated_lombok() {
        // just a simple test , compile should never
        // let lombok create an instance that is not well formed
        LombokEntity entity = new LombokEntity();
        Assertions.assertNotNull(DelegatesService.getInstance().getEntityClass(entity.getClass()));
    }
}
