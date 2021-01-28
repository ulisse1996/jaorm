package io.jaorm.dsl.impl;

import io.jaorm.dsl.common.IntermediateWhere;
import io.jaorm.dsl.common.Where;
import io.jaorm.entity.sql.SqlParameter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WhereImpl<T> implements Where<T>, IntermediateWhere<T> {

    private static final String WHERE = " WHERE ";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String COLUMN_CAN_T_BE_NULL = "Column can't be null";
    private static final String VALUE_CAN_T_BE_NULL = "Value can't be null !";
    private final List<WhereClause> clauses;
    private final SelectImpl.EndSelectImpl<T> parent;
    private boolean linkedCause;

    public WhereImpl(SelectImpl.EndSelectImpl<T> endSelect, String column, boolean or) {
        this.parent = endSelect;
        this.clauses = new ArrayList<>();
        this.clauses.add(new WhereClause(column, or));
    }

    @Override
    public IntermediateWhere<T> eq(Object val) {
        Objects.requireNonNull(val, VALUE_CAN_T_BE_NULL);
        return operation(Operation.EQUALS, val);
    }

    @Override
    public IntermediateWhere<T> ne(Object val) {
        Objects.requireNonNull(val, VALUE_CAN_T_BE_NULL);
        return operation(Operation.NOT_EQUALS, val);
    }

    @Override
    public IntermediateWhere<T> lt(Object val) {
        Objects.requireNonNull(val, VALUE_CAN_T_BE_NULL);
        return operation(Operation.LESS_THAN, val);
    }

    @Override
    public IntermediateWhere<T> gt(Object val) {
        Objects.requireNonNull(val, VALUE_CAN_T_BE_NULL);
        return operation(Operation.GREATER_THAN, val);
    }

    @Override
    public IntermediateWhere<T> le(Object val) {
        Objects.requireNonNull(val, VALUE_CAN_T_BE_NULL);
        return operation(Operation.LESS_EQUALS, val);
    }

    @Override
    public IntermediateWhere<T> ge(Object val) {
        Objects.requireNonNull(val, VALUE_CAN_T_BE_NULL);
        return operation(Operation.GREATER_EQUALS, val);
    }

    @Override
    public IntermediateWhere<T> in(Iterable<?> iterable) {
        Objects.requireNonNull(iterable, "Iterable can't be null !");
        return operation(Operation.IN, null, iterable);
    }

    @Override
    public IntermediateWhere<T> notIn(Iterable<?> iterable) {
        Objects.requireNonNull(iterable, "Iterable can't be null");
        return operation(Operation.NOT_IN, null, iterable);
    }

    @Override
    public IntermediateWhere<T> isNull() {
        return operation(Operation.IS_NULL, null);
    }

    @Override
    public IntermediateWhere<T> isNotNull() {
        return operation(Operation.IS_NOT_NULL, null);
    }

    @Override
    public IntermediateWhere<T> like(LikeType type, String val) {
        Objects.requireNonNull(val, VALUE_CAN_T_BE_NULL);
        Objects.requireNonNull(type, "LikeType can't be null !");
        return operation(Operation.LIKE, val, null, type);
    }

    private IntermediateWhere<T> operation(Operation operation, Object val) {
        return operation(operation, val, null);
    }

    private IntermediateWhere<T> operation(Operation operation, Object val, Iterable<?> iterable) {
        return operation(operation, val, iterable, null);
    }

    private IntermediateWhere<T> operation(Operation operation, Object val, Iterable<?> iterable, LikeType type) {
        getCurrent().operation = operation;
        getCurrent().val = val;
        getCurrent().iterable = iterable;
        getCurrent().likeType = type;
        return this;
    }

    private WhereClause getCurrent() {
        WhereClause last = this.clauses.get(this.clauses.size() - 1);
        if (linkedCause) {
            last = last.linked.get(last.linked.size() - 1);
        }
        return last;
    }

    @Override
    public Where<T> where(String column) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        this.linkedCause = false;
        this.clauses.add(new WhereClause(column, false));
        return this;
    }

    @Override
    public Where<T> orWhere(String column) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        this.linkedCause = false;
        this.clauses.add(new WhereClause(column, true));
        return this;
    }

    @Override
    public T read() {
        return this.parent.read();
    }

    @Override
    public Optional<T> readOpt() {
        return this.parent.readOpt();
    }

    @Override
    public List<T> readAll() {
        return this.parent.readAll();
    }

    @Override
    public Where<T> and(String column) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        WhereClause cause = new WhereClause(column, false);
        getCurrent().linked.add(cause);
        this.linkedCause = true;
        return this;
    }

    @Override
    public Where<T> or(String column) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        WhereClause cause = new WhereClause(column, true);
        getCurrent().linked.add(cause);
        this.linkedCause = true;
        return this;
    }

    public boolean hasClauses() {
        return !this.clauses.isEmpty();
    }

    public String getSql() {
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        for (WhereClause clause : clauses) {
            if (first) {
                builder.append(WHERE);
                first = false;
            } else {
                builder.append(clause.or ? OR : AND);
            }
            builder.append("(");
            buildClause(builder, clause);
            builder.append(")");
        }

        return builder.toString();
    }

    private void buildClause(StringBuilder builder, WhereClause clause) {
        builder.append(clause.column).append(evaluateOperation(clause));
        if (!clause.linked.isEmpty()) {
            for (WhereClause inner : clause.linked) {
                builder.append(inner.or ? OR : AND)
                        .append(inner.column).append(evaluateOperation(inner));
            }
        }
    }

    private String evaluateOperation(WhereClause clause) {
        switch (clause.operation) {
            case EQUALS:
            case NOT_EQUALS:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_EQUALS:
            case GREATER_EQUALS:
                return clause.operation.getValue() + "?";
            case IS_NULL:
                return " IS NULL";
            case IS_NOT_NULL:
                return " IS NOT NULL";
            case IN:
            case NOT_IN:
                String wildcards = String.join(",", Collections.nCopies(getSize(clause.iterable), "?"));
                return String.format(" %s (%s)", Operation.IN.equals(clause.operation) ? "IN" : "NOT IN", wildcards);
            case LIKE:
                return clause.likeType.getValue();
            default:
                throw new IllegalArgumentException("Can't find operation type");
        }
    }

    private int getSize(Iterable<?> iterable) {
        return (int) iterable.spliterator().estimateSize();
    }

    public List<SqlParameter> getParameters() {
        return getParameters(this.clauses);
    }

    private List<SqlParameter> getParameters(List<WhereClause> clauses) {
        List<SqlParameter> parameters = new ArrayList<>();
        for (WhereClause clause : clauses) {
            if (clause.iterable != null) {
                parameters.addAll(StreamSupport.stream(clause.iterable.spliterator(), false)
                        .map(SqlParameter::new)
                        .collect(Collectors.toList()));
            } else if (clause.val != null) {
                parameters.add(new SqlParameter(clause.val));
            }
            if (!clause.linked.isEmpty()) {
                parameters.addAll(getParameters(clause.linked));
            }
        }

        return parameters;
    }

    @Override
    public String toString() {
        return this.parent.toString();
    }

    private static class WhereClause {

        private final String column;
        private final boolean or;
        private final List<WhereClause> linked;
        private Iterable<?> iterable;
        private LikeType likeType;
        private Operation operation;
        private Object val;

        public WhereClause(String column, boolean or) {
            this.column = column;
            this.or = or;
            this.linked = new ArrayList<>();
        }
    }
}
