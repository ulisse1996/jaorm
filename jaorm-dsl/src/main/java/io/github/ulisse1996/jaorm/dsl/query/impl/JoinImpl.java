package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.common.SelectedOn;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateJoin;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.On;
import io.github.ulisse1996.jaorm.dsl.query.enums.JoinType;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class JoinImpl<T, R, L> implements On<T, R>, IntermediateJoin<T, R, L> {

    private static final String OR = " OR ";
    private static final String AND = " AND ";
    private static final String COLUMN_CAN_T_BE_NULL = "Column can't be null";
    private static final String ALIAS_COLUMN = "%s.%s";
    private static final String VALUE_CAN_T_BE_NULL = "Value can't be null !";
    private final JoinType joinType;
    private final SelectedImpl<T, ?> parent;
    private final String alias;
    private final String table;
    private final List<OnImpl> ons;
    private OnImpl lastOn;

    public JoinImpl(Class<R> table, SelectedImpl<T, ?> parent, JoinType joinType, String alias) {
        EntityDelegate<?> delegate = DelegatesService.getInstance().searchDelegate(table)
                .get();
        this.table = delegate.getTable();
        this.parent = parent;
        this.joinType = joinType;
        this.alias = alias;
        this.ons = new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N> IntermediateJoin<T, R, N> on(SqlColumn<R, N> column) {
        OnImpl on = new OnImpl(false, column);
        this.ons.add(on);
        this.lastOn = on;
        return (IntermediateJoin<T, R, N>) this;
    }

    @Override
    public SelectedOn<T, R> eq(SqlColumn<?, L> column) {
        return operation(Operation.EQUALS, column, null);
    }

    @Override
    public SelectedOn<T, R> ne(SqlColumn<?, L> column) {
        return operation(Operation.NOT_EQUALS, column, null);
    }

    @Override
    public SelectedOn<T, R> lt(SqlColumn<?, L> column) {
        return operation(Operation.LESS_THAN, column, null);
    }

    @Override
    public SelectedOn<T, R> gt(SqlColumn<?, L> column) {
        return operation(Operation.GREATER_THAN, column, null);
    }

    @Override
    public SelectedOn<T, R> le(SqlColumn<?, L> column) {
        return operation(Operation.LESS_EQUALS, column, null);
    }

    @Override
    public SelectedOn<T, R> ge(SqlColumn<?, L> column) {
        return operation(Operation.GREATER_EQUALS, column, null);
    }

    @Override
    public SelectedOn<T, R> eq(L value) {
        return operation(Operation.EQUALS, value);
    }

    @Override
    public SelectedOn<T, R> ne(L value) {
        return operation(Operation.NOT_EQUALS, value);
    }

    @Override
    public SelectedOn<T, R> lt(L value) {
        return operation(Operation.LESS_THAN, value);
    }

    @Override
    public SelectedOn<T, R> gt(L value) {
        return operation(Operation.GREATER_THAN, value);
    }

    @Override
    public SelectedOn<T, R> le(L value) {
        return operation(Operation.LESS_EQUALS, value);
    }

    @Override
    public SelectedOn<T, R> ge(L value) {
        return operation(Operation.GREATER_EQUALS, value);
    }

    @Override
    public SelectedOn<T, R> eq(SqlColumn<?, L> column, String alias) {
        return operation(Operation.EQUALS, column, alias);
    }

    @Override
    public SelectedOn<T, R> ne(SqlColumn<?, L> column, String alias) {
        return operation(Operation.NOT_EQUALS, column, alias);
    }

    @Override
    public SelectedOn<T, R> lt(SqlColumn<?, L> column, String alias) {
        return operation(Operation.LESS_THAN, column, alias);
    }

    @Override
    public SelectedOn<T, R> gt(SqlColumn<?, L> column, String alias) {
        return operation(Operation.GREATER_THAN, column, alias);
    }

    @Override
    public SelectedOn<T, R> le(SqlColumn<?, L> column, String alias) {
        return operation(Operation.LESS_EQUALS, column, alias);
    }

    @Override
    public SelectedOn<T, R> ge(SqlColumn<?, L> column, String alias) {
        return operation(Operation.GREATER_EQUALS, column, alias);
    }

    @Override
    public SelectedOn<T, R> equalsTo(SqlColumn<?, L> column) {
        return operation(Operation.EQUALS, column, null);
    }

    @Override
    public SelectedOn<T, R> notEqualsTo(SqlColumn<?, L> column) {
        return operation(Operation.NOT_EQUALS, column, null);
    }

    @Override
    public SelectedOn<T, R> lessThan(SqlColumn<?, L> column) {
        return operation(Operation.LESS_THAN, column, null);
    }

    @Override
    public SelectedOn<T, R> greaterThan(SqlColumn<?, L> column) {
        return operation(Operation.GREATER_THAN, column, null);
    }

    @Override
    public SelectedOn<T, R> lessOrEqualsTo(SqlColumn<?, L> column) {
        return operation(Operation.LESS_EQUALS, column, null);
    }

    @Override
    public SelectedOn<T, R> greaterOrEqualsTo(SqlColumn<?, L> column) {
        return operation(Operation.GREATER_EQUALS, column, null);
    }

    @Override
    public SelectedOn<T, R> equalsTo(L value) {
        return operation(Operation.EQUALS, value);
    }

    @Override
    public SelectedOn<T, R> notEqualsTo(L value) {
        return operation(Operation.NOT_EQUALS, value);
    }

    @Override
    public SelectedOn<T, R> lessThan(L value) {
        return operation(Operation.LESS_THAN, value);
    }

    @Override
    public SelectedOn<T, R> greaterThan(L value) {
        return operation(Operation.GREATER_THAN, value);
    }

    @Override
    public SelectedOn<T, R> lessOrEqualsTo(L value) {
        return operation(Operation.LESS_EQUALS, value);
    }

    @Override
    public SelectedOn<T, R> greaterOrEqualsTo(L value) {
        return operation(Operation.GREATER_EQUALS, value);
    }

    @Override
    public SelectedOn<T, R> equalsTo(SqlColumn<?, L> column, String alias) {
        return operation(Operation.EQUALS, column, alias);
    }

    @Override
    public SelectedOn<T, R> notEqualsTo(SqlColumn<?, L> column, String alias) {
        return operation(Operation.NOT_EQUALS, column, alias);
    }

    @Override
    public SelectedOn<T, R> lessThan(SqlColumn<?, L> column, String alias) {
        return operation(Operation.LESS_THAN, column, alias);
    }

    @Override
    public SelectedOn<T, R> greaterThan(SqlColumn<?, L> column, String alias) {
        return operation(Operation.GREATER_THAN, column, alias);
    }

    @Override
    public SelectedOn<T, R> lessOrEqualsTo(SqlColumn<?, L> column, String alias) {
        return operation(Operation.LESS_EQUALS, column, alias);
    }

    @Override
    public SelectedOn<T, R> greaterOrEqualsTo(SqlColumn<?, L> column, String alias) {
        return operation(Operation.GREATER_EQUALS, column, alias);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedOn<T, R> like(LikeType type, SqlColumn<?, String> column) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        this.lastOn.targetColumn = column;
        this.lastOn.likeType = type;
        this.lastOn.operation = Operation.LIKE;
        return (SelectedOn<T, R>) this.parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedOn<T, R> notLike(LikeType type, SqlColumn<?, String> column) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        this.lastOn.targetColumn = column;
        this.lastOn.likeType = type;
        this.lastOn.operation = Operation.NOT_LIKE;
        return (SelectedOn<T, R>) this.parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedOn<T, R> like(LikeType type, String value) {
        Objects.requireNonNull(value, VALUE_CAN_T_BE_NULL);
        this.lastOn.likeType = type;
        this.lastOn.operation = Operation.LIKE;
        this.lastOn.value = value;
        return (SelectedOn<T, R>) this.parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedOn<T, R> notLike(LikeType type, String value) {
        Objects.requireNonNull(value, VALUE_CAN_T_BE_NULL);
        this.lastOn.likeType = type;
        this.lastOn.operation = Operation.NOT_LIKE;
        this.lastOn.value = value;
        return (SelectedOn<T, R>) this.parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedOn<T, R> like(LikeType type, SqlColumn<?, String> column, String alias) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        this.lastOn.targetColumn = column;
        this.lastOn.likeType = type;
        this.lastOn.targetAlias = alias;
        this.lastOn.operation = Operation.LIKE;
        return (SelectedOn<T, R>) this.parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedOn<T, R> notLike(LikeType type, SqlColumn<?, String> column, String alias) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        this.lastOn.targetColumn = column;
        this.lastOn.likeType = type;
        this.lastOn.operation = Operation.NOT_LIKE;
        this.lastOn.targetAlias = alias;
        return (SelectedOn<T, R>) this.parent;
    }

    private SelectedOn<T,R> operation(Operation operation, SqlColumn<?,L> column, String alias) {
        return operation(operation, column, alias, null);
    }

    private SelectedOn<T,R> operation(Operation operation, Object value) {
        return operation(operation, null, null, value);
    }

    @SuppressWarnings("unchecked")
    private SelectedOn<T,R> operation(Operation operation, SqlColumn<?,L> column, String alias, Object value) {
        if (value == null) {
            Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        } else {
            Objects.requireNonNull(value, VALUE_CAN_T_BE_NULL);
        }
        this.lastOn.operation = operation;
        this.lastOn.targetColumn = column;
        this.lastOn.targetAlias = alias;
        this.lastOn.value = value;
        return (SelectedOn<T, R>) this.parent;
    }

    @SuppressWarnings("unchecked")
    public <M, N> IntermediateJoin<T,N, M> orOn(SqlColumn<N, M> column) {
        OnImpl on = new OnImpl(true, column);
        this.ons.add(on);
        this.lastOn = on;
        return (IntermediateJoin<T, N, M>) this;
    }

    public String asString(boolean caseInsensitiveLike) {
        StringBuilder sql = new StringBuilder(this.joinType.getValue() + this.table + asAlias() + " ON");
        boolean first = true;
        for (OnImpl clause : this.ons) {
            if (first) {
                first = false;
                sql.append(" ");
            } else {
                sql.append(clause.or ? OR : AND);
            }
            sql.append("(");
            buildClause(sql, clause, caseInsensitiveLike);
            sql.append(")");
        }

        return sql.toString();
    }

    private void buildClause(StringBuilder builder, OnImpl clause, boolean caseInsensitiveLike) {
        builder.append(String.format(ALIAS_COLUMN, asAliasOn(), clause.column.getName())).append(evaluateOperation(clause, caseInsensitiveLike));
    }

    private String asAliasOn() {
        return this.alias != null ? this.alias : this.table;
    }

    private String evaluateOperation(OnImpl clause, boolean caseInsensitiveLike) {
        switch (clause.operation) {
            case EQUALS:
            case NOT_EQUALS:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_EQUALS:
            case GREATER_EQUALS:
                if (clause.value == null) {
                    return clause.operation.getValue() + String.format(ALIAS_COLUMN, fromOrAlias(clause), clause.targetColumn.getName());
                } else {
                    return clause.operation.getValue() + "?";
                }
            case NOT_LIKE:
                if (clause.value == null) {
                    return " NOT LIKE" + clause.likeType.format(fromOrAlias(clause), clause.targetColumn.getName(), caseInsensitiveLike);
                } else {
                    return " NOT LIKE" + clause.likeType.getValue(this.parent.isCaseInsensitiveLike());
                }
            case LIKE:
                if (clause.value == null) {
                    return " LIKE" + clause.likeType.format(fromOrAlias(clause), clause.targetColumn.getName(), caseInsensitiveLike);
                } else {
                    return " LIKE" + clause.likeType.getValue(this.parent.isCaseInsensitiveLike());
                }
            case IN:
            case NOT_IN:
            case IS_NULL:
            case IS_NOT_NULL:
            default:
                throw new IllegalArgumentException("Can't find operation type");
        }
    }

    private String fromOrAlias(OnImpl clause) {
        return clause.targetAlias != null ? clause.targetAlias : this.parent.getTable();
    }

    private String asAlias() {
        return alias != null ? VendorSpecific.getSpecific(AliasesSpecific.class).convertToAlias(alias) : "";
    }

    public Stream<SqlParameter> getParameters() {
        return this.ons.stream()
                .filter(o -> o.value != null)
                .map(o -> o.value)
                .map(SqlParameter::new);
    }

    private static class OnImpl {

        private final SqlColumn<?, ?> column;
        private final boolean or;
        private LikeType likeType;
        private String targetAlias;
        private Operation operation;
        private SqlColumn<?, ?> targetColumn;
        private Object value;

        private OnImpl(boolean or, SqlColumn<?, ?> column) {
            Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
            this.or = or;
            this.column = column;
        }
    }
}
