package io.github.ulisse1996.jaorm.cache;

import io.github.ulisse1996.jaorm.cache.impl.CacheConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

class StandardConfigurationTest {

    @Test
    void should_create_standard_cache() {
        CacheConfiguration configuration = (CacheConfiguration) StandardConfiguration.INSTANCE;
        Assertions.assertEquals(StandardConfiguration.STANDARD_SIZE, getField("size", configuration));
        Assertions.assertEquals(StandardConfiguration.STANDARD_AFTER_ACCESS, getField("afterAccess", configuration));
        Assertions.assertEquals(StandardConfiguration.STANDARD_AFTER_WRITE, getField("afterWrite", configuration));
        Assertions.assertEquals(false, getField("weakKeys", configuration));
        Assertions.assertEquals(false, getField("weakValues", configuration));
        Assertions.assertEquals(false, getField("softValues", configuration));
    }

    private Object getField(String fieldName, CacheConfiguration configuration) {
        try {
            Field field = CacheConfiguration.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(configuration);
        } catch (Exception ex) {
            Assertions.fail(ex);
            return null;
        }
    }
}
