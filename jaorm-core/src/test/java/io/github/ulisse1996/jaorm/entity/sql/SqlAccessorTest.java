package io.github.ulisse1996.jaorm.entity.sql;

import io.github.ulisse1996.jaorm.entity.converter.ConverterPair;
import io.github.ulisse1996.jaorm.spi.ConverterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

class SqlAccessorTest {

    @Test
    void should_find_custom_accessor() {
        try (MockedStatic<ConverterService> mk = Mockito.mockStatic(ConverterService.class)) {
            mk.when(ConverterService::getInstance)
                    .thenReturn(new ConverterService() {
                        @Override
                        public Map<Class<?>, ConverterPair<?, ?>> getConverters() {
                            return Collections.singletonMap(Object.class, new ConverterPair<>(Object.class, null));
                        }
                    });
            Assertions.assertDoesNotThrow(() -> SqlAccessor.find(Object.class));
        }
    }
}
