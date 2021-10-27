package io.github.ulisse1996.jaorm.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class EntityMapperTest {

    @Test
    void should_create_mappers_from_builder() {
        EntityMapper.Builder<?> builder = new EntityMapper.Builder<>();
        builder.add("NAME1", String.class, (entity, value) -> {}, entity -> null, true, true);
        builder.add("NAME2", BigDecimal.class, (entity, value) -> {}, entity -> null, true, true);
        EntityMapper<?> mapper = builder.build();
        Assertions.assertEquals(2, mapper.getMappers().size());
        Assertions.assertAll(
                () -> Assertions.assertEquals("NAME1", mapper.getMappers().get(0).getName()),
                () -> Assertions.assertEquals("NAME2", mapper.getMappers().get(1).getName()),
                () -> Assertions.assertEquals(String.class, mapper.getMappers().get(0).getType()),
                () -> Assertions.assertEquals(BigDecimal.class, mapper.getMappers().get(1).getType())
        );
    }

}
