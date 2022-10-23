package io.github.ulisse1996.jaorm.dsl.query.impl.simple;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.enums.JoinType;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.*;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleJoinImpl implements SimpleOn, SimpleSelectedOn {

    private final String table;
    private final String alias;
    private final SimpleSelectedImpl parent;
    private final JoinType type;
    private final List<SimpleOnImpl<?>> ons;

    public SimpleJoinImpl(String table, String alias, SimpleSelectedImpl parent, JoinType type) {
        this.table = table;
        this.alias = alias;
        this.parent = parent;
        this.type = type;
        this.ons = new ArrayList<>();
    }

    SimpleSelectedImpl getParent() {
        return parent;
    }

    @Override
    public <L> IntermediateSimpleJoin<L> on(SqlColumn<?, L> column) {
        return this.on(column, null);
    }

    @Override
    public <L> IntermediateSimpleJoin<L> on(SqlColumn<?, L> column, String alias) {
        return addOn(column, alias, false);
    }

    @Override
    public <L> IntermediateSimpleJoin<L> andOn(SqlColumn<?, L> column) {
        return andOn(column, null);
    }

    @Override
    public <L> IntermediateSimpleJoin<L> andOn(SqlColumn<?, L> onColumn, String alias) {
        return addOn(onColumn, alias, false);
    }

    @Override
    public <L> IntermediateSimpleJoin<L> orOn(SqlColumn<?, L> column) {
        return orOn(column, null);
    }

    @Override
    public <L> IntermediateSimpleJoin<L> orOn(SqlColumn<?, L> column, String alias) {
        return addOn(column, alias, true);
    }

    private <L> IntermediateSimpleJoin<L> addOn(SqlColumn<?, L> column, String alias, boolean or) {
        SimpleOnImpl<L> simpleOn = new SimpleOnImpl<>(column, alias, this, or);
        this.ons.add(simpleOn);
        return simpleOn;
    }

    @Override
    public SimpleOn join(String table) {
        return this.join(table, null);
    }

    @Override
    public SimpleOn leftJoin(String table) {
        return this.leftJoin(table, null);
    }

    @Override
    public SimpleOn rightJoin(String table) {
        return this.rightJoin(table, null);
    }

    @Override
    public SimpleOn fullJoin(String table) {
        return this.fullJoin(table, null);
    }

    @Override
    public SimpleOn join(String table, String alias) {
        return this.parent.join(table, alias);
    }

    @Override
    public SimpleOn leftJoin(String table, String alias) {
        return this.parent.leftJoin(table, alias);
    }

    @Override
    public SimpleOn rightJoin(String table, String alias) {
        return this.parent.rightJoin(table, alias);
    }

    @Override
    public SimpleOn fullJoin(String table, String alias) {
        return this.parent.fullJoin(table, alias);
    }

    public Pair<String, List<SqlParameter>> doBuild() {
        List<SqlParameter> params = new ArrayList<>();
        StringBuilder builder = new StringBuilder(
            this.alias != null
                    ? String.format("%s %s %s ON ", this.type.getValue().trim(), this.table, this.alias)
                    : String.format("%s %s ON ", this.type.getValue().trim(), this.table)
        );
        for (int i = 0; i < this.ons.size(); i++) {
            SimpleOnImpl<?> on = this.ons.get(i);
            if (i != 0) {
                builder.append(on.or ? " OR " : " AND ");
            }
            builder.append(
                    on.alias != null
                    ? String.format("%s.%s", on.alias, on.column.getName())
                    : String.format("%s", on.column.getName())
            );
            builder.append(evaluateOperation(on));
            if (on.value != null) {
                params.add(new SqlParameter(on.value));
            }
        }

        return new Pair<>(builder.toString(), params);
    }

    private String evaluateOperation(SimpleOnImpl<?> on) {
        boolean caseInsensitive = this.parent.getConfiguration().isCaseInsensitive();
        switch (on.operation) {
            case EQUALS:
            case NOT_EQUALS:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_EQUALS:
            case GREATER_EQUALS:
                if (on.value == null) {
                    return on.operation.getValue() + checkAlias(on.onAlias, on.onColumn);
                } else {
                    return on.operation.getValue() + "?";
                }
            case LIKE:
                if (on.value == null) {
                    return " LIKE" + getLikeFormat(on, caseInsensitive);
                } else {
                    return " LIKE" + on.type.getValue(caseInsensitive);
                }
            case NOT_LIKE:
                if (on.value == null) {
                    return " NOT LIKE" + getLikeFormat(on, caseInsensitive);
                } else {
                    return " NOT LIKE" + on.type.getValue(caseInsensitive);
                }
            case IN:
            case NOT_IN:
            case IS_NULL:
            case IS_NOT_NULL:
            default:
                throw new IllegalArgumentException("Can't find operation type");
        }
    }

    private String getLikeFormat(SimpleOnImpl<?> on, boolean caseInsensitive) {
        return on.onAlias != null
                ? on.type.format(on.onAlias, on.onColumn.getName(), caseInsensitive)
                : on.type.format(on.onColumn.getName(), caseInsensitive);
    }

    private String checkAlias(String onAlias, SqlColumn<?,?> onColumn) {
        if (onAlias != null) {
            return String.format("%s.%s", onAlias, onColumn.getName());
        } else {
            return onColumn.getName();
        }
    }

    @Override
    public <R> R read(Class<R> klass) {
        return this.parent.read(klass);
    }

    @Override
    public <R> Optional<R> readOpt(Class<R> klass) {
        return this.parent.readOpt(klass);
    }

    @Override
    public <R> List<R> readAll(Class<R> klass) {
        return this.parent.readAll(klass);
    }

    @Override
    public WithProjectionResult union(WithProjectionResult union) {
        return this.parent.union(union);
    }

    @Override
    public SimpleSelectedOn withConfiguration(QueryConfig config) {
        this.parent.setConfiguration(config);
        return this;
    }

    @Override
    public SimpleOrder orderBy(OrderType type, SqlColumn<?, ?> column, String alias) {
        return this.parent.orderBy(type, column, alias);
    }

    @Override
    public SimpleOrder orderBy(OrderType type, SqlColumn<?, ?> column) {
        return orderBy(type, column, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> where(SqlColumn<?, R> column) {
        return where(column, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> where(SqlColumn<?, R> column, String alias) {
        return this.parent.where(column, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> where(VendorFunction<R> function) {
        return where(function, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> where(VendorFunction<R> function, String alias) {
        return this.parent.where(function, alias);
    }

    private static class SimpleOnImpl<L> implements IntermediateSimpleJoin<L> {

        private final SqlColumn<?, L> column;
        private final String alias;
        private final SimpleJoinImpl parent;
        private final boolean or;
        private Operation operation;
        private L value;
        private SqlColumn<?, L> onColumn;
        private String onAlias;
        private LikeType type;

        public SimpleOnImpl(SqlColumn<?, L> column, String alias, SimpleJoinImpl parent, boolean or) {
            this.column = column;
            this.alias = alias;
            this.parent = parent;
            this.or = or;
        }

        @Override
        public SimpleSelectedOn eq(SqlColumn<?, L> column) {
            return eq(column, null);
        }

        @Override
        public SimpleSelectedOn ne(SqlColumn<?, L> column) {
            return ne(column, null);
        }

        @Override
        public SimpleSelectedOn lt(SqlColumn<?, L> column) {
            return lt(column, null);
        }

        @Override
        public SimpleSelectedOn gt(SqlColumn<?, L> column) {
            return gt(column, null);
        }

        @Override
        public SimpleSelectedOn le(SqlColumn<?, L> column) {
            return le(column, null);
        }

        @Override
        public SimpleSelectedOn ge(SqlColumn<?, L> column) {
            return ge(column, null);
        }

        @Override
        public SimpleSelectedOn like(LikeType type, SqlColumn<?, String> column) {
            return like(type, column, null);
        }

        @Override
        public SimpleSelectedOn notLike(LikeType type, SqlColumn<?, String> column) {
            return notLike(type, column, null);
        }

        @Override
        public SimpleSelectedOn eq(SqlColumn<?, L> column, String alias) {
            return operation(column, alias, null, Operation.EQUALS);
        }

        @Override
        public SimpleSelectedOn ne(SqlColumn<?, L> column, String alias) {
            return operation(column, alias, null, Operation.NOT_EQUALS);
        }

        @Override
        public SimpleSelectedOn lt(SqlColumn<?, L> column, String alias) {
            return operation(column, alias, null, Operation.LESS_THAN);
        }

        @Override
        public SimpleSelectedOn gt(SqlColumn<?, L> column, String alias) {
            return operation(column, alias, null, Operation.GREATER_THAN);
        }

        @Override
        public SimpleSelectedOn le(SqlColumn<?, L> column, String alias) {
            return operation(column, alias, null, Operation.LESS_EQUALS);
        }

        @Override
        public SimpleSelectedOn ge(SqlColumn<?, L> column, String alias) {
            return operation(column, alias, null, Operation.GREATER_EQUALS);
        }

        @Override
        @SuppressWarnings("unchecked")
        public SimpleSelectedOn like(LikeType type, SqlColumn<?, String> column, String alias) {
            return operation((SqlColumn<?, L>) column, alias, null, Operation.LIKE, type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public SimpleSelectedOn notLike(LikeType type, SqlColumn<?, String> column, String alias) {
            return operation((SqlColumn<?, L>) column, alias, null, Operation.NOT_LIKE, type);
        }

        @Override
        public SimpleSelectedOn eq(L value) {
            return operation(null, null, value, Operation.EQUALS);
        }

        @Override
        public SimpleSelectedOn ne(L value) {
            return operation(null, null, value, Operation.NOT_EQUALS);
        }

        @Override
        public SimpleSelectedOn lt(L value) {
            return operation(null, null, value, Operation.LESS_THAN);
        }

        @Override
        public SimpleSelectedOn gt(L value) {
            return operation(null, null, value, Operation.GREATER_THAN);
        }

        @Override
        public SimpleSelectedOn le(L value) {
            return operation(null, null, value, Operation.LESS_EQUALS);
        }

        @Override
        public SimpleSelectedOn ge(L value) {
            return operation(null, null, value, Operation.GREATER_EQUALS);
        }

        @Override
        @SuppressWarnings("unchecked")
        public SimpleSelectedOn like(LikeType type, String value) {
            return operation(null, null, (L) value, Operation.LIKE, type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public SimpleSelectedOn notLike(LikeType type, String value) {
            return operation(null, null, (L) value, Operation.NOT_LIKE, type);
        }

        private SimpleSelectedOn operation(SqlColumn<?, L> column, String alias, L value, Operation operation, LikeType type) {
            this.type = type;
            return this.operation(column, alias, value, operation);
        }

        private SimpleSelectedOn operation(SqlColumn<?, L> column, String alias, L value, Operation operation) {
            this.onColumn = column;
            this.onAlias = alias;
            this.value = value;
            this.operation = operation;
            return this.parent;
        }
    }
}
