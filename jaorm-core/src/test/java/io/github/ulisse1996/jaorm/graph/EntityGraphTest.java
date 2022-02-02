package io.github.ulisse1996.jaorm.graph;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.DelegatesMock;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class EntityGraphTest {

    @Mock private DelegatesService delegatesService;
    @Mock private EntityDelegate<?> delegate;
    @Mock private EntityMapper<?> mapper;
    @Mock private QueryRunner runner;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @Test
    void should_return_empty_fetch() throws SQLException {
        EntityGraph<DelegatesMock.MyEntity> graph = buildGraph();
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDel.when(DelegatesService::getInstance).thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any(Class.class)))
                    .then(invocation -> (Supplier<EntityDelegate<?>>) () -> delegate);
            Mockito.when(delegate.getSelectables()).thenReturn(new String[] {"COL1", "COL2"});
            Mockito.when(delegate.getTable()).thenReturn("TABLE");
            Mockito.when(delegate.getEntityMapper()).then(invocation -> mapper);
            Mockito.when(mapper.getKeys(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.getConnection(Mockito.any()))
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.any()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next()).thenReturn(false);
            Optional<DelegatesMock.MyEntity> result = graph.fetchOpt(new DelegatesMock.MyEntity());
            Assertions.assertFalse(result.isPresent());
        }
    }

    @Test
    void should_throw_exception_for_fetch() throws SQLException {
        EntityGraph<DelegatesMock.MyEntity> graph = buildGraph();
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDel.when(DelegatesService::getInstance).thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any(Class.class)))
                    .then(invocation -> (Supplier<EntityDelegate<?>>) () -> delegate);
            Mockito.when(delegate.getSelectables()).thenReturn(new String[] {"COL1", "COL2"});
            Mockito.when(delegate.getTable()).thenReturn("TABLE");
            Mockito.when(delegate.getEntityMapper()).then(invocation -> mapper);
            Mockito.when(mapper.getKeys(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.getConnection(Mockito.any()))
                    .thenThrow(SQLException.class);
            try {
                graph.fetch(new DelegatesMock.MyEntity());
            } catch (JaormSqlException ex) {
                Assertions.assertTrue(ex.getCause() instanceof SQLException);
            }
        }
    }

    @Test
    void should_map_graph() throws SQLException {
        EntityGraph<DelegatesMock.MyEntity> graph = buildGraph();
        try (MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDel.when(DelegatesService::getInstance).thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any(Class.class)))
                    .then(invocation -> (Supplier<EntityDelegate<?>>) () -> delegate);
            Mockito.when(delegate.getSelectables()).thenReturn(new String[] {"COL1", "COL2"});
            Mockito.when(delegate.getTable()).thenReturn("TABLE");
            Mockito.when(delegate.getEntityMapper()).then(invocation -> mapper);
            Mockito.when(mapper.getKeys(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.getConnection(Mockito.any()))
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.any()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.next()).thenReturn(true, false);
            Mockito.when(mapper.containsGraphResult(Mockito.any(), Mockito.any()))
                    .thenReturn(true);
            graph.fetch(new DelegatesMock.MyEntity());
            Mockito.verify(mapper, Mockito.times(4))
                    .mapForGraph(Mockito.any(), Mockito.any(), Mockito.any());
        }
    }

    @SuppressWarnings("unchecked")
    private EntityGraph<DelegatesMock.MyEntity> buildGraph() {
        AtomicReference<List<Object>> atomicReference = new AtomicReference<>();
        EntityGraph.Builder<DelegatesMock.MyEntity> builder = EntityGraph.builder(DelegatesMock.MyEntity.class);
        builder.addChild(
                Object.class, "JOIN", NodeType.SINGLE, (entity, value) -> {}, entity -> null, "A"
        );
        builder.addChild(
                Object.class, "JOIN", NodeType.OPTIONAL, (entity, value) -> {}, entity -> null, "B"
        );
        builder.addChild(
                Object.class, "JOIN", NodeType.COLLECTION, (entity, value) -> atomicReference.set((List<Object>) value), entity -> atomicReference.get(), "C"
        );
        return builder.build();
    }
}
