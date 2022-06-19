package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;

class ProjectionsServiceTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {
        try {
            Field field = ProjectionsService.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<ProjectionsService> instance = (Singleton<ProjectionsService>) field.get(null);
            instance.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }
}
