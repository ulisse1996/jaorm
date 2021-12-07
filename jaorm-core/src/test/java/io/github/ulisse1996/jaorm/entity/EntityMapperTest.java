package io.github.ulisse1996.jaorm.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

@ExtendWith(MockitoExtension.class)
class EntityMapperTest {

    @Mock private ColumnGetter<Object, Object> getter;
    @Mock private ColumnSetter<Object, Object> setter;
    @Mock private ResultSet resultSet;

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
                () -> Assertions.assertEquals(BigDecimal.class, mapper.getMappers().get(1).getType()),
                () -> Assertions.assertTrue(mapper.getMappers().get(0).isKey()),
                () -> Assertions.assertTrue(mapper.getMappers().get(1).isKey())
        );
    }

    @Test
    void should_map_graph() throws SQLException {
        EntityMapper.Builder<Object> builder = new EntityMapper.Builder<>();
        builder.add("NAME1", String.class, setter, getter, true, true);
        builder.add("NAME2", BigDecimal.class, setter, getter, true, true);
        EntityMapper<Object> mapper = builder.build();
        Mockito.when(resultSet.getString("TABLE.NAME1"))
                .thenReturn("testStr");
        Mockito.when(resultSet.getBigDecimal("TABLE.NAME2"))
                .thenReturn(BigDecimal.ONE);
        mapper.mapForGraph(Object::new, resultSet, "TABLE");
        Mockito.verify(setter)
                .accept(Mockito.any(), Mockito.eq("testStr"));
        Mockito.verify(setter)
                .accept(Mockito.any(), Mockito.eq(BigDecimal.ONE));
    }

    @Test
    void should_return_false_for_graph_result() throws SQLException {
        EntityMapper.Builder<Object> builder = new EntityMapper.Builder<>();
        builder.add("NAME1", String.class, setter, getter, true, true);
        builder.add("NAME2", long.class, setter, getter, true, true);
        EntityMapper<Object> mapper = builder.build();
        Mockito.when(resultSet.getString("TABLE.NAME1"))
                .thenReturn("testStr");
        Mockito.when(resultSet.getLong("TABLE.NAME2"))
                .thenReturn(0L);
        Mockito.when(resultSet.wasNull())
                .thenReturn(false, true);
        Assertions.assertFalse(mapper.containsGraphResult(resultSet, "TABLE"));
    }

    @Test
    void should_return_true_for_graph_result() throws SQLException {
        EntityMapper.Builder<Object> builder = new EntityMapper.Builder<>();
        builder.add("NAME1", String.class, setter, getter, true, true);
        builder.add("NAME2", long.class, setter, getter, true, true);
        EntityMapper<Object> mapper = builder.build();
        Mockito.when(resultSet.getString("TABLE.NAME1"))
                .thenReturn("testStr");
        Mockito.when(resultSet.getLong("TABLE.NAME2"))
                .thenReturn(0L);
        Assertions.assertTrue(mapper.containsGraphResult(resultSet, "TABLE"));
    }
}
