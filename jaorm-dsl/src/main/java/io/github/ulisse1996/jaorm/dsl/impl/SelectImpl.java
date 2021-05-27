package io.github.ulisse1996.jaorm.dsl.impl;

import io.github.ulisse1996.jaorm.dsl.common.*;
import io.github.ulisse1996.jaorm.dsl.select.Select;
import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectImpl implements Select {

    private static final String COLUMN_CAN_T_BE_NULL = "Column can't be null !";
    private static final String JOINED_TABLE_CAN_T_BE_NULL = "Joined Table can't be null !";
    private static final int NOT_ACTIVE = -1;

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

    static class EndSelectImpl<T, R, M> implements EndSelect<T>, Order<T>, Fetch<T>, Offset<T> {

        final Class<T> klass;
        final String[] columns;
        final String from;
        private WhereImpl<T, R, M> where;
        private OrderImpl<T> order;
        private final List<JoinImpl<?, ?>> joins;
        private int limit;
        private int offset;

        public EndSelectImpl(String from, Class<T> klass, String[] columns) {
            this.from = from;
            this.klass = klass;
            this.columns = columns;
            this.joins = new ArrayList<>();
            this.limit = NOT_ACTIVE;
            this.offset = NOT_ACTIVE;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <L> Where<T, L> where(SqlColumn<T, L> column) {
            checkColumn(column);
            this.where = new WhereImpl<>(this, column.getName(),false, (ValueConverter<?, R>) column.getConverter());
            return (Where<T, L>) this.where;
        }

        void checkColumn(SqlColumn<T, ?> column) {
            Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
            checkColumn(column.getName());
        }

        private void checkColumn(String column) {
            Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
            if (this.klass != null && Stream.of(columns).map(String::trim).noneMatch(column::equals)) {
                throw new IllegalArgumentException(String.format("Can't find column %s in columns %s", column, Arrays.toString(columns)));
            }
        }

        @Override
        public Join<T> join(Class<?> table) {
            Objects.requireNonNull(table, JOINED_TABLE_CAN_T_BE_NULL);
            return addJoin(new JoinImpl<>(this, table, JoinType.JOIN));
        }

        @Override
        public Join<T> leftJoin(Class<?> table) {
            Objects.requireNonNull(table, JOINED_TABLE_CAN_T_BE_NULL);
            return addJoin(new JoinImpl<>(this, table, JoinType.LEFT_JOIN));
        }

        @Override
        public Join<T> rightJoin(Class<?> table) {
            Objects.requireNonNull(table, JOINED_TABLE_CAN_T_BE_NULL);
            return addJoin(new JoinImpl<>(this, table, JoinType.RIGHT_JOIN));
        }

        @Override
        public Join<T> fullJoin(Class<?> table) {
            Objects.requireNonNull(table, JOINED_TABLE_CAN_T_BE_NULL);
            return addJoin(new JoinImpl<>(this, table, JoinType.FULL_JOIN));
        }

        private Join<T> addJoin(JoinImpl<T, ?> join) {
            this.joins.add(join);
            return join;
        }

        @Override
        public Fetch<T> limit(int row) {
            if (row <= 0) {
                throw new IllegalArgumentException("Row can't be less or equals to 0");
            }
            this.limit = row;
            return this;
        }

        @Override
        public Offset<T> offset(int row) {
            if (row <= 0) {
                throw new IllegalArgumentException("Row can't be less or equals to 0");
            }
            this.offset = row;
            return this;
        }

        @Override
        public final Order<T> orderBy(OrderType type, SqlColumn<T, ?> column) {
            Objects.requireNonNull(type, "Order type can't be null !");
            Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
            appendOrder(type, column);
            return this;
        }

        private void appendOrder(OrderType type, SqlColumn<T, ?> column) {
            if (this.order == null) {
                this.order = new OrderImpl<>(this.from);
            }

            this.order.add(type, column);
        }

        @Override
        public T read() {
            Pair<String, List<SqlParameter>> pair = buildSql();
            return QueryRunner.getInstance(this.klass).read(klass, pair.getKey(), pair.getValue());
        }

        @Override
        public Optional<T> readOpt() {
            Pair<String, List<SqlParameter>> pair = buildSql();
            return QueryRunner.getInstance(klass).readOpt(klass, pair.getKey(), pair.getValue())
                    .toOptional();
        }

        @Override
        public List<T> readAll() {
            Pair<String, List<SqlParameter>> pair = buildSql();
            return QueryRunner.getInstance(klass).readAll(klass, pair.getKey(), pair.getValue());
        }

        Pair<String, List<SqlParameter>> buildSql() {
            List<SqlParameter> parameters = new ArrayList<>();
            List<String> cols = Stream.of(columns)
                    .map(s -> from + "." + s)
                    .collect(Collectors.toList());
            StringBuilder select = new StringBuilder("SELECT " + String.join(", ", cols) + " FROM " + from);
            if (!joins.isEmpty()) {
                for (JoinImpl<?,?> join : joins) {
                    select.append(join.getSql());
                    parameters.addAll(join.getParameters());
                }
            }
            if (where != null && where.hasClauses()) {
                select.append(where.getSql());
                parameters.addAll(where.getParameters());
            }
            if (order != null) {
                select.append(" ORDER BY ").append(order.getSql());
            }

            buildLimitOffset(select);

            return new Pair<>(select.toString(), parameters);
        }

        private void buildLimitOffset(StringBuilder select) {
            if (limit != NOT_ACTIVE || offset != NOT_ACTIVE) {
                LimitOffsetSpecific specific = VendorSpecific.getSpecific(LimitOffsetSpecific.class);
                if (limit != NOT_ACTIVE && offset != NOT_ACTIVE) {
                    select.append(specific.convertOffSetLimitSupport(limit, offset));
                } else {
                    select.append(specific.convertOffSetLimitSupport(limit));
                }
            }
        }

        @Override
        public String toString() {
            Pair<String, List<SqlParameter>> pair = buildSql();
            return pair.getKey() + " [" +
                    pair.getValue().stream().map(par -> String.valueOf(par.getVal())).collect(Collectors.joining(",")) + "]";
        }
    }
}
