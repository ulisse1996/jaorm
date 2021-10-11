package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateWhere;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WhereImpl<T, R> implements IntermediateWhere<T, R> {

    private static final String OBJECT_CANT_BE_NULL = "Object can't be null !";
    private static final String WHERE = " WHERE ";
    protected static final String AND_CLAUSE = " AND ";
    protected static final String OR_CLAUSE = " OR ";

    private final SqlColumn<?, R> column;
    private final SelectedImpl<T, ?> parent;
    protected final boolean or;
    protected final List<WhereImpl<T, ?>> links;
    private final String alias;
    private R value;
    private Operation operation;
    private Iterable<R> iterable;
    private LikeType likeType;

    public WhereImpl(SqlColumn<?, R> column, SelectedImpl<T, ?> parent, boolean or, String alias) {
        this.column = column;
        this.parent = parent;
        this.or = or;
        this.links = new ArrayList<>();
        this.alias = alias;
    }

    @Override
    public SelectedWhere<T> eq(R val) {
        return operation(val, Operation.EQUALS);
    }

    @Override
    public SelectedWhere<T> ne(R val) {
        return operation(val, Operation.NOT_EQUALS);
    }

    @Override
    public SelectedWhere<T> lt(R val) {
        return operation(val, Operation.LESS_THAN);
    }

    @Override
    public SelectedWhere<T> gt(R val) {
        return operation(val, Operation.GREATER_THAN);
    }

    @Override
    public SelectedWhere<T> le(R val) {
        return operation(val, Operation.LESS_EQUALS);
    }

    @Override
    public SelectedWhere<T> ge(R val) {
        return operation(val, Operation.GREATER_EQUALS);
    }

    @Override
    public SelectedWhere<T> equalsTo(R val) {
        return operation(val, Operation.EQUALS);
    }

    @Override
    public SelectedWhere<T> notEqualsTo(R val) {
        return operation(val, Operation.NOT_EQUALS);
    }

    @Override
    public SelectedWhere<T> lessThan(R val) {
        return operation(val, Operation.LESS_THAN);
    }

    @Override
    public SelectedWhere<T> greaterThan(R val) {
        return operation(val, Operation.GREATER_THAN);
    }

    @Override
    public SelectedWhere<T> lessOrEqualsTo(R val) {
        return operation(val, Operation.LESS_EQUALS);
    }

    @Override
    public SelectedWhere<T> greaterOrEqualsTo(R val) {
        return operation(val, Operation.GREATER_EQUALS);
    }

    @Override
    public SelectedWhere<T> in(Iterable<R> iterable) {
        Objects.requireNonNull(iterable, OBJECT_CANT_BE_NULL);
        this.operation = Operation.IN;
        this.iterable = iterable;
        return this.parent;
    }

    @Override
    public SelectedWhere<T> notIn(Iterable<R> iterable) {
        Objects.requireNonNull(iterable, OBJECT_CANT_BE_NULL);
        this.operation = Operation.NOT_IN;
        this.iterable = iterable;
        return this.parent;
    }

    @Override
    public SelectedWhere<T> isNull() {
        this.operation = Operation.IS_NULL;
        return this.parent;
    }

    @Override
    public SelectedWhere<T> isNotNull() {
        this.operation = Operation.IS_NOT_NULL;
        return this.parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedWhere<T> like(LikeType type, String val) {
        assertIsString();
        this.likeType = type;
        return operation((R) val, Operation.LIKE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedWhere<T> notLike(LikeType type, String val) {
        assertIsString();
        this.likeType = type;
        return operation((R) val, Operation.NOT_LIKE);
    }

    protected void assertIsString() {
        if (!column.getType().equals(String.class)) {
            throw new IllegalArgumentException("Can't use like without a column that match String.class");
        }
    }

    public void addLinked(WhereImpl<T, ?> where) {
        this.links.add(where);
    }

    private SelectedWhere<T> operation(R value, Operation operation) {
        Objects.requireNonNull(value, OBJECT_CANT_BE_NULL);
        this.operation = operation;
        this.value = value;
        return this.parent;
    }

    public String asString(boolean first, boolean caseInsensitiveLike) {
        StringBuilder builder = new StringBuilder();
        if (first) {
            builder.append(WHERE);
        } else {
            builder.append(this.or ? OR_CLAUSE : AND_CLAUSE);
        }

        builder.append("(");
        buildClause(builder, caseInsensitiveLike);
        builder.append(")");

        return builder.toString();
    }

    protected void buildClause(StringBuilder builder, boolean caseInsensitiveLike) {
        String format = caseInsensitiveLike && this.likeType != null ? "UPPER(%s.%s)" : "%s.%s";
        builder.append(String.format(format, getFrom(this), this.column.getName())).append(evaluateOperation(this, caseInsensitiveLike));
        buildLinked(builder, caseInsensitiveLike);
    }

    protected void buildLinked(StringBuilder builder, boolean caseInsensitiveLike) {
        if (!this.links.isEmpty()) {
            for (WhereImpl<?, ?> inner : this.links) {
                buildInner(builder, caseInsensitiveLike, inner);
            }
        }
    }

    protected void buildInner(StringBuilder builder, boolean caseInsensitiveLike, WhereImpl<?, ?> inner) {
        String innerFormat = caseInsensitiveLike && inner.likeType != null ? "UPPER(%s.%s)" : "%s.%s";
        builder.append(inner.or ? OR_CLAUSE : AND_CLAUSE)
                .append(String.format(innerFormat, getFrom(inner), inner.column.getName())).append(evaluateOperation(inner, caseInsensitiveLike));
    }

    protected String evaluateOperation(WhereImpl<?, ?> clause, boolean caseInsensitiveLike) {
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
            case NOT_LIKE:
                return " NOT LIKE" + clause.likeType.getValue(caseInsensitiveLike);
            case LIKE:
                return " LIKE" + clause.likeType.getValue(caseInsensitiveLike);
            default:
                throw new IllegalArgumentException("Can't find operation type");
        }
    }

    private int getSize(Iterable<?> iterable) {
        return (int) iterable.spliterator().estimateSize();
    }

    protected String getFrom(WhereImpl<?, ?> where) {
        if (where.alias == null || where.alias.isEmpty()) {
            return where.parent.getTable();
        } else {
            return where.alias;
        }
    }

    public Stream<SqlParameter> getParameters() {
        return Stream.concat(
                value != null
                        ? Stream.of(new SqlParameter(value))
                        : StreamSupport.stream(iterable.spliterator(), false).map(SqlParameter::new),
                links.stream().flatMap(WhereImpl::getParameters)
        );
    }

}
