package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.mapping.*;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.GeneratorsService;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.util.ClassChecker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EntityQueryRunner extends QueryRunner {

    @Override
    public boolean isCompatible(Class<?> klass) {
        return isDelegate(klass);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R read(Class<R> entity, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        SupplierPair supplierPair = getSupplierPair(entity);
        try (Connection connection = getConnection(
                supplierPair.projectionSupplier == null
                        ? DelegatesService.getInstance().getTableInfo(entity)
                        : supplierPair.projectionSupplier.get().asTableInfo()
                );
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSetExecutor executor = new ResultSetExecutor(preparedStatement, params)) {
            executor.getResultSet().next();
            if (supplierPair.projectionSupplier == null) {
                EntityDelegate<?> entityDelegate = supplierPair.delegateSupplier.get();
                entityDelegate.setEntity(executor.getResultSet());
                return (R) entityDelegate;
            } else {
                ProjectionDelegate delegate = supplierPair.projectionSupplier.get();
                delegate.setEntity(executor.getResultSet());
                return (R) delegate;
            }
        } catch (SQLException ex) {
            logger.error(String.format("Error during read for entity %s ", entity)::toString, ex);
            throw new JaormSqlException(ex);
        }
    }

    private SupplierPair getSupplierPair(Class<?> klass) {
        Supplier<EntityDelegate<?>> delegateSupplier = null;
        Supplier<ProjectionDelegate> projectionSupplier = ProjectionsService.getInstance()
                .getProjections()
                .entrySet()
                .stream()
                .filter(el -> ClassChecker.isAssignable(el.getKey(), klass))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
        if (projectionSupplier == null) {
            delegateSupplier = DelegatesService.getInstance().searchDelegate(klass);
        }
        return new SupplierPair(delegateSupplier, projectionSupplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Result<R> readOpt(Class<R> entity, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        SupplierPair supplierPair = getSupplierPair(entity);
        try (Connection connection = getConnection(
                supplierPair.projectionSupplier == null
                        ? DelegatesService.getInstance().getTableInfo(entity)
                        : supplierPair.projectionSupplier.get().asTableInfo()
             );
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSetExecutor executor = new ResultSetExecutor(preparedStatement, params)) {
            if (executor.getResultSet().next()) {
                if (supplierPair.projectionSupplier == null) {
                    EntityDelegate<?> entityDelegate = supplierPair.delegateSupplier.get();
                    entityDelegate.setEntity(executor.getResultSet());
                    return (Result<R>) Result.of(entityDelegate);
                } else {
                    ProjectionDelegate delegate = supplierPair.projectionSupplier.get();
                    delegate.setEntity(executor.getResultSet());
                    return (Result<R>) Result.of(delegate);
                }
            } else {
                return Result.empty();
            }
        } catch (SQLException ex) {
            logger.error(String.format("Error during readOpt for entity %s ", entity)::toString, ex);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> List<R> readAll(Class<R> entity, String query, List<SqlParameter> params) {
        logger.logSql(query, params);
        List<R> values = new ArrayList<>();
        SupplierPair supplierPair = getSupplierPair(entity);
        try (Connection connection = getConnection(
                supplierPair.projectionSupplier == null
                        ? DelegatesService.getInstance().getTableInfo(entity)
                        : supplierPair.projectionSupplier.get().asTableInfo()
            );
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSetExecutor executor = new ResultSetExecutor(preparedStatement, params)) {
            while (executor.getResultSet().next()) {
                if (supplierPair.projectionSupplier == null) {
                    EntityDelegate<?> entityDelegate = supplierPair.delegateSupplier.get();
                    entityDelegate.setEntity(executor.getResultSet());
                    values.add((R) entityDelegate);
                } else {
                    ProjectionDelegate delegate = supplierPair.projectionSupplier.get();
                    delegate.setEntity(executor.getResultSet());
                    values.add((R) delegate);
                }
            }

            return values;
        } catch (SQLException ex) {
            logger.error(String.format("Error during readAll for entity %s ", entity)::toString, ex);
            throw new JaormSqlException(ex);
        }
    }

    @Override
    public <R> Stream<R> readStream(Class<R> entity, String query, List<SqlParameter> params) {
        return produceIterableResult(entity, query, params, ResultSetStream::new).getStream();
    }

    @Override
    public <R> Cursor<R> readCursor(Class<R> klass, String query, List<SqlParameter> parameters) {
        return produceIterableResult(klass, query, parameters, JaormCursor::new);
    }

    private <T, R> T produceIterableResult(Class<R> entity, String query, List<SqlParameter> params, JaormIterableResultProducer<T, R> producer) {
        logger.logSql(query, params);
        Connection connection = EmptyClosable.instance(Connection.class);
        PreparedStatement preparedStatement = EmptyClosable.instance(PreparedStatement.class);
        SqlExecutor executor = EmptyClosable.instance(SqlExecutor.class);
        try {
            SupplierPair supplierPair = getSupplierPair(entity);
            connection = getConnection(
                    supplierPair.projectionSupplier == null
                            ? DelegatesService.getInstance().getTableInfo(entity)
                            : supplierPair.projectionSupplier.get().asTableInfo()
            );
            preparedStatement = connection.prepareStatement(query);
            executor = new ResultSetExecutor(preparedStatement, params);
            return producer.produce(connection, preparedStatement, executor, getMapper(supplierPair));
        } catch (SQLException ex) {
            SqlUtil.silentClose(executor, preparedStatement, connection);
            logger.error(String.format("Error during readStream for entity %s ", entity)::toString, ex);
            throw new JaormSqlException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <R> ThrowingFunction<ResultSet, R, SQLException> getMapper(SupplierPair supplierPair) {
        return rs -> {
            if (supplierPair.projectionSupplier != null) {
                ProjectionDelegate delegate = supplierPair.projectionSupplier.get();
                delegate.setEntity(rs);
                return (R) delegate;
            }
            EntityDelegate<R> delegate = (EntityDelegate<R>) supplierPair.delegateSupplier.get();
            delegate.setEntity(rs);
            return (R) delegate;
        };
    }

    @Override
    public TableRow read(String query, List<SqlParameter> params) {
        throw new UnsupportedOperationException("Read with TableRow is not supported");
    }

    @Override
    public Optional<TableRow> readOpt(String query, List<SqlParameter> params) {
        throw new UnsupportedOperationException("ReadOpt with TableRow is not supported");
    }

    @Override
    public Stream<TableRow> readStream(String query, List<SqlParameter> params) {
        throw new UnsupportedOperationException("ReadStream with TableRow is not supported");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R insert(R entity, String query, List<SqlParameter> params) {
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getInstance().searchDelegate(entity.getClass());
        EntityDelegate<R> delegate = (EntityDelegate<R>) delegateSupplier.get();
        delegate.setFullEntity(entity);
        Map<String, Class<?>> columns = delegate.getAutoGenerated();
        if (needPreGeneration(entity.getClass())) {
            Map<String, Object> generated = generateKeys(columns, entity.getClass());
            delegate.setAutoGenerated(generated);
            params = SqlParameter.argumentsAsParameters(DelegatesService.getInstance()
                    .asInsert(entity, generated).getValues());
        }
        Map<String,Object> generated = doUpdate(entity.getClass(), query, params, columns);
        delegate.setAutoGenerated(generated);
        return (R) delegate;
    }

    private boolean needPreGeneration(Class<?> aClass) {
        return GeneratorsService.getInstance().needGeneration(aClass);
    }

    private Map<String, Object> generateKeys(Map<String, Class<?>> columns, Class<?> entityClass) {
        Map<String, Object> generated = new HashMap<>();
        GeneratorsService service = GeneratorsService.getInstance();
        for (Map.Entry<String, Class<?>> entry : columns.entrySet()) {
            boolean canGenerate = service.canGenerateValue(entityClass, entry.getKey());

            if (canGenerate) {
                try {
                    generated.put(entry.getKey(), service.generate(entityClass, entry.getKey(), entry.getValue()));
                } catch (SQLException ex) {
                    throw new JaormSqlException(ex);
                }
            }
        }
        generated.keySet().forEach(columns::remove);
        return generated;
    }

    @Override
    public int update(String query, List<SqlParameter> params) {
        return doSimpleUpdate(query, params);
    }

    @Override
    public int delete(String query, List<SqlParameter> params) {
        return doSimpleUpdate(query, params);
    }

    @Override
    public <R> List<R> updateWithBatch(Class<?> entityClass, String updateSql, List<R> entities) {
        List<List<SqlParameter>> parameters = entities.stream()
                .map(e -> Stream.concat(DelegatesService.getInstance().asArguments(e).asSqlParameters().stream(),
                                DelegatesService.getInstance().asWhere(e).asSqlParameters().stream())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        doBatchUpdate(entityClass, updateSql, parameters, Collections.emptyMap());
        return entities;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> List<R> insertWithBatch(Class<?> entityClass, String sql, List<R> entities) {
        Supplier<EntityDelegate<?>> delegateSupplier = DelegatesService.getInstance().searchDelegate(entityClass);
        List<List<SqlParameter>> allParams = new ArrayList<>();
        Map<String, Class<?>> columns = new HashMap<>();
        List<R> results = new ArrayList<>();
        for (R entity : entities) {
            EntityDelegate<R> delegate = (EntityDelegate<R>) delegateSupplier.get();
            columns = delegate.getAutoGenerated();
            List<SqlParameter> params;
            delegate.setFullEntity(entity);
            if (needPreGeneration(entity.getClass())) {
                Map<String, Object> generated = generateKeys(columns, entity.getClass());
                delegate.setAutoGenerated(generated);
                params = SqlParameter.argumentsAsParameters(DelegatesService.getInstance()
                        .asInsert(entity, generated).getValues());
            } else {
                params = DelegatesService.getInstance()
                        .asInsert(entity).asSqlParameters();
            }
            results.add((R) delegate);
            allParams.add(params);
        }
        List<Map<String, Object>> generated = doBatchUpdate(entityClass, sql, allParams, columns);
        if (!generated.isEmpty()) {
            IntStream.range(0, entities.size())
                    .forEach(i -> {
                        EntityDelegate<R> delegate = (EntityDelegate<R>) results.get(i);
                        delegate.setAutoGenerated(generated.get(i));
                    });
        }
        return results;
    }

    private static class SupplierPair {

        private final Supplier<EntityDelegate<?>> delegateSupplier;
        private final Supplier<ProjectionDelegate> projectionSupplier;

        public SupplierPair(Supplier<EntityDelegate<?>> delegateSupplier, Supplier<ProjectionDelegate> projectionSupplier) {
            this.delegateSupplier = delegateSupplier;
            this.projectionSupplier = projectionSupplier;
        }
    }
}
