package io.jaorm.dsl.impl;

import io.jaorm.dsl.common.EndSelect;
import io.jaorm.dsl.common.Where;
import io.jaorm.dsl.select.Select;
import io.jaorm.dsl.util.Pair;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.SqlColumn;
import io.jaorm.entity.converter.ValueConverter;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.spi.DelegatesService;
import io.jaorm.spi.QueryRunner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectImpl implements Select {

    @Override
    public <T> EndSelect<T> select(Class<T> klass) {
        EntityDelegate<?> delegate = DelegatesService.getInstance().searchDelegate(klass).get();
        String[] columns = delegate.getSelectables();
        String table = delegate.getTable();
        return selectFrom(table, klass, columns);
    }
    
    private <T> EndSelect<T> selectFrom(String from , Class<T> klass, String[] columns) {
        return new EndSelectImpl<>(from, klass, columns);
    }

    static class EndSelectImpl<T, R> implements EndSelect<T> {

        final Class<T> klass;
        final String[] columns;
        private final String from;
        private WhereImpl<T, R> where;

        public EndSelectImpl(String from, Class<T> klass, String[] columns) {
            this.from = from;
            this.klass = klass;
            this.columns = columns;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <L> Where<T, L> where(SqlColumn<T, L> column) {
            checkColumn(column);
            this.where = new WhereImpl<>(this, column.getName(),false, (ValueConverter<?, R>) column.getConverter());
            return (Where<T, L>) this.where;
        }

        private void checkColumn(SqlColumn<T, ?> column) {
            Objects.requireNonNull(column, "Column can't be null !");
            checkColumn(column.getName());
        }

        private void checkColumn(String column) {
            Objects.requireNonNull(column, "Column can't be null !");
            if (this.klass != null && Stream.of(columns).map(String::trim).noneMatch(column::equals)) {
                throw new IllegalArgumentException(String.format("Can't find column %s in columns %s", column, Arrays.toString(columns)));
            }
        }

        @Override
        public <L> Where<T, L> orWhere(SqlColumn<T, L> column) {
            throw new UnsupportedOperationException("Can't use orWhere before an AND !");
        }

        @Override
        public T read() {
            Pair<String, List<SqlParameter>> pair = buildSql();
            return QueryRunner.getInstance(this.klass).read(klass, pair.getKey(), pair.getValue());
        }

        @Override
        public Optional<T> readOpt() {
            Pair<String, List<SqlParameter>> pair = buildSql();
            return QueryRunner.getInstance(klass).readOpt(klass, pair.getKey(), pair.getValue());
        }

        @Override
        public List<T> readAll() {
            Pair<String, List<SqlParameter>> pair = buildSql();
            return QueryRunner.getInstance(klass).readAll(klass, pair.getKey(), pair.getValue());
        }

        Pair<String, List<SqlParameter>> buildSql() {
            List<SqlParameter> parameters = Collections.emptyList();
            String select = "SELECT " + String.join(", ", columns) + " FROM " + from;
            if (where != null && where.hasClauses()) {
                select += where.getSql();
                parameters = where.getParameters();
            }

            return new Pair<>(select, parameters);
        }

        @Override
        public String toString() {
            Pair<String, List<SqlParameter>> pair = buildSql();
            return pair.getKey() + " [" +
                    pair.getValue().stream().map(par -> String.valueOf(par.getVal())).collect(Collectors.joining(",")) + "]";
        }
    }
}
