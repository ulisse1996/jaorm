package io.jaorm.dsl.impl;

import io.jaorm.QueryRunner;
import io.jaorm.dsl.common.EndSelect;
import io.jaorm.dsl.common.Where;
import io.jaorm.dsl.select.Select;
import io.jaorm.dsl.util.Pair;
import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.sql.SqlParameter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectImpl implements Select {

    @Override
    public <T> EndSelect<T> select(Class<T> klass) {
        EntityDelegate<?> delegate = DelegatesService.getCurrent().searchDelegate(klass).get();
        String[] columns = delegate.getSelectables();
        String table = delegate.getTable();
        return select(table, klass, columns);
    }
    
    private <T> EndSelect<T> select(String from , Class<T> klass, String[] columns) {
        return new EndSelectImpl<>(from, klass, columns);
    }

    static class EndSelectImpl<T> implements EndSelect<T> {

        private final Class<T> klass;
        private final String[] columns;
        private final String from;
        private WhereImpl<T> where;

        public EndSelectImpl(String from, Class<T> klass, String[] columns) {
            this.from = from;
            this.klass = klass;
            this.columns = columns;
        }

        @Override
        public Where<T> where(String column) {
            checkColumn(column);
            this.where = new WhereImpl<>(this, column,false);
            return this.where;
        }

        private void checkColumn(String column) {
            Objects.requireNonNull(column, "Column can't be null !");
            if (this.klass != null && Stream.of(columns).map(String::trim).noneMatch(column::equals)) {
                throw new IllegalArgumentException(String.format("Can't find column %s in columns %s", column, Arrays.toString(columns)));
            }
        }

        @Override
        public Where<T> orWhere(String column) {
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
            String select = "SELECT (" + String.join(", ", columns) + ") FROM " + from;
            if (where.hasClauses()) {
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
