package io.github.ulisse1996.jaorm.dsl.impl;

import io.github.ulisse1996.jaorm.dsl.common.*;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JoinImpl<T, R> implements Join<T>, On<T, R>, IntermediateJoin<T> {

    private static final String OR = " OR ";
    private static final String AND = " AND ";
    private static final String COLUMN_CAN_T_BE_NULL = "Column can't be null";
    private static final String VALUE_CAN_T_BE_NULL = "Value can't be null !";
    private static final String ALIAS_COLUMN = "%s.%s";
    private final String joinTable;
    private final JoinType joinType;
    private final SelectImpl.EndSelectImpl<T, ?, ?> parent;
    private final List<JoinImpl.OnClause> clauses;
    private final List<String> joinTableColumns;
    private boolean linkedCause;

    public JoinImpl(SelectImpl.EndSelectImpl<T, ?, ?> parent, Class<?> joinClass, JoinType type) {
        EntityDelegate<?> delegate = DelegatesService.getInstance().searchDelegate(joinClass).get();
        this.parent = parent;
        this.joinTable = delegate.getTable();
        this.joinTableColumns = Arrays.asList(delegate.getSelectables());
        this.joinType = type;
        this.clauses = new ArrayList<>();
    }

    public String getJoinTable() {
        return joinTable;
    }

    public List<String> getJoinTableColumns() {
        return joinTableColumns;
    }

    private JoinImpl.OnClause getCurrent() {
        JoinImpl.OnClause last = this.clauses.get(this.clauses.size() - 1);
        if (linkedCause) {
            last = last.linked.get(last.linked.size() - 1);
        }
        return last;
    }

    private <L> void checkColumn(SqlColumn<L, ?> column) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        checkColumn(column.getName());
    }

    private void checkColumn(String column) {
        Objects.requireNonNull(column, COLUMN_CAN_T_BE_NULL);
        if (joinTableColumns.stream().map(String::trim).noneMatch(column::equals)) {
            throw new IllegalArgumentException(String.format("Can't find column %s in columns %s", column, joinTableColumns));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> On<T, M> on(SqlColumn<T, M> column) {
        this.linkedCause = false;
        this.parent.checkColumn(column);
        this.clauses.add(new OnClause(column));
        return (On<T, M>) this;
    }

    @Override
    public <L> IntermediateJoin<T> eq(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.EQUALS, column);
    }

    @Override
    public <L> IntermediateJoin<T> ne(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.NOT_EQUALS, column);
    }

    @Override
    public <L> IntermediateJoin<T> lt(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.LESS_THAN, column);
    }

    @Override
    public <L> IntermediateJoin<T> gt(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.GREATER_THAN, column);
    }

    @Override
    public <L> IntermediateJoin<T> le(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.LESS_EQUALS, column);
    }

    @Override
    public <L> IntermediateJoin<T> ge(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.GREATER_EQUALS, column);
    }

    @Override
    public <L> IntermediateJoin<T> equalsTo(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.EQUALS, column);
    }

    @Override
    public <L> IntermediateJoin<T> notEqualsTo(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.NOT_EQUALS, column);
    }

    @Override
    public <L> IntermediateJoin<T> lessThan(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.LESS_THAN, column);
    }

    @Override
    public <L> IntermediateJoin<T> greaterThan(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.GREATER_THAN, column);
    }

    @Override
    public <L> IntermediateJoin<T> lessOrEqualsTo(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.LESS_EQUALS, column);
    }

    @Override
    public <L> IntermediateJoin<T> greaterOrEqualsTo(SqlColumn<L, R> column) {
        checkColumn(column);
        return operation(Operation.GREATER_EQUALS, column);
    }

    @Override
    public <L> IntermediateJoin<T> inColumns(Iterable<SqlColumn<L, R>> iterable) {
        Objects.requireNonNull(iterable, "Iterable can't be null !");
        iterable.forEach(this::checkColumn);
        return operationIterable(Operation.IN, iterable);
    }

    @Override
    public <L> IntermediateJoin<T> notInColumns(Iterable<SqlColumn<L, R>> iterable) {
        Objects.requireNonNull(iterable, "Iterable can't be null !");
        iterable.forEach(this::checkColumn);
        return operationIterable(Operation.NOT_IN, iterable);
    }

    @Override
    public <L> IntermediateJoin<T> like(LikeType type, SqlColumn<L, String> column) {
        checkColumn(column);
        return operation(Operation.LIKE, column, type);
    }

    @Override
    public <L> IntermediateJoin<T> notLike(LikeType type, SqlColumn<L, String> column) {
        checkColumn(column);
        return operation(Operation.NOT_LIKE, column, type);
    }

    @Override
    public IntermediateJoin<T> eq(R val) {
        return operation(Operation.EQUALS, val);
    }

    @Override
    public IntermediateJoin<T> ne(R val) {
        return operation(Operation.NOT_EQUALS, val);
    }

    @Override
    public IntermediateJoin<T> lt(R val) {
        return operation(Operation.LESS_THAN, val);
    }

    @Override
    public IntermediateJoin<T> gt(R val) {
        return operation(Operation.GREATER_THAN, val);
    }

    @Override
    public IntermediateJoin<T> le(R val) {
        return operation(Operation.LESS_EQUALS, val);
    }

    @Override
    public IntermediateJoin<T> ge(R val) {
        return operation(Operation.GREATER_EQUALS, val);
    }

    @Override
    public IntermediateJoin<T> equalsTo(R val) {
        return operation(Operation.EQUALS, val);
    }

    @Override
    public IntermediateJoin<T> notEqualsTo(R val) {
        return operation(Operation.NOT_EQUALS, val);
    }

    @Override
    public IntermediateJoin<T> lessThan(R val) {
        return operation(Operation.LESS_THAN, val);
    }

    @Override
    public IntermediateJoin<T> greaterThan(R val) {
        return operation(Operation.GREATER_THAN, val);
    }

    @Override
    public IntermediateJoin<T> lessOrEqualsTo(R val) {
        return operation(Operation.LESS_EQUALS, val);
    }

    @Override
    public IntermediateJoin<T> greaterOrEqualsTo(R val) {
        return operation(Operation.GREATER_EQUALS, val);
    }

    @Override
    public IntermediateJoin<T> in(Iterable<R> iterable) {
        return operation(Operation.IN, iterable);
    }

    @Override
    public IntermediateJoin<T> notIn(Iterable<R> iterable) {
        return operation(Operation.NOT_IN, iterable);
    }

    @Override
    public IntermediateJoin<T> isNull() {
        return operation(Operation.IS_NULL);
    }

    @Override
    public IntermediateJoin<T> isNotNull() {
        return operation(Operation.IS_NOT_NULL);
    }

    @Override
    public IntermediateJoin<T> like(LikeType type, String val) {
        Objects.requireNonNull(val, VALUE_CAN_T_BE_NULL);
        return operation(Operation.LIKE, val, type);
    }

    @Override
    public IntermediateJoin<T> notLike(LikeType type, String val) {
        Objects.requireNonNull(val, VALUE_CAN_T_BE_NULL);
        return operation(Operation.NOT_LIKE, val, type);
    }

    private <L> IntermediateJoin<T> operation(Operation operation, SqlColumn<L, String> column, LikeType type) {
        getCurrent().likeType = type;
        getCurrent().joinColumn = column.getName();
        return operation(operation);
    }

    private IntermediateJoin<T> operation(Operation operation, String val, LikeType type) {
        getCurrent().likeType = type;
        return operation(operation, val);
    }

    private <L, M> IntermediateJoin<T> operationIterable(Operation operation, Iterable<SqlColumn<L, M>> columns) {
        return operation(operation, null, null, columns);
    }

    private IntermediateJoin<T> operation(Operation operation, Iterable<R> iterable) {
        getCurrent().iterable = iterable;
        return operation(operation);
    }

    private IntermediateJoin<T> operation(Operation operation) {
        return operation(operation, null,null, null);
    }

    private <L> IntermediateJoin<T> operation(Operation operation, SqlColumn<L, R> column) {
        return operation(operation, null, column, null);
    }

    private IntermediateJoin<T> operation(Operation operation, Object val) {
        return operation(operation, val, null, null);
    }

    @SuppressWarnings("unchecked")
    private <L, M> IntermediateJoin<T> operation(Operation operation, Object val, SqlColumn<L, R> column,
                                              Iterable<SqlColumn<L, M>> columns) {
        if (columns != null) {
            getCurrent().columns = StreamSupport.stream(columns.spliterator(), false)
                    .map(SqlColumn::getName)
                    .collect(Collectors.toList());
        }
        getCurrent().operation = operation;
        if (column != null) {
            getCurrent().joinColumn = column.getName();
        }
        getCurrent().value = ((ValueConverter<?,R>)getCurrent().converter).toSql((R)val);
        return this;
    }

    @Override
    public <L> Where<T, L> where(SqlColumn<T, L> column) {
        return this.parent.where(column);
    }

    @Override
    public <A, L> Where<T, L> whereJoinColumn(SqlColumn<A, L> column) {
        return this.parent.whereJoinColumn(column);
    }

    @Override
    public Join<T> join(Class<?> table) {
        return this.parent.join(table);
    }

    @Override
    public Join<T> leftJoin(Class<?> table) {
        return this.parent.leftJoin(table);
    }

    @Override
    public Join<T> rightJoin(Class<?> table) {
        return this.parent.rightJoin(table);
    }

    @Override
    public Join<T> fullJoin(Class<?> table) {
        return this.parent.fullJoin(table);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> On<T, M> and(SqlColumn<T, M> column) {
        this.parent.checkColumn(column);
        getCurrent().linked.add(new OnClause(column));
        this.linkedCause = true;
        return (On<T, M>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> On<T, M> or(SqlColumn<T, M> column) {
        this.parent.checkColumn(column);
        getCurrent().linked.add(new OnClause(column, true));
        this.linkedCause = true;
        return (On<T, M>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> On<T, M> andOn(SqlColumn<T, M> column) {
        this.parent.checkColumn(column);
        this.linkedCause = false;
        this.clauses.add(new OnClause(column));
        return (On<T, M>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> On<T, M> orOn(SqlColumn<T, M> column) {
        this.parent.checkColumn(column);
        this.linkedCause = false;
        this.clauses.add(new OnClause(column, true));
        return (On<T, M>) this;
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
    public long count() {
        return this.parent.count();
    }

    public String getSql(boolean caseInsensitiveLike) {
        StringBuilder sql = new StringBuilder(this.joinType.getValue() + this.joinTable + " ON");
        boolean first = true;
        for (OnClause clause : clauses) {
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

    private void buildClause(StringBuilder builder, OnClause clause, boolean caseInsensitiveLike) {
        builder.append(String.format(ALIAS_COLUMN, this.parent.from, clause.joinedColumn)).append(evaluateOperation(clause, caseInsensitiveLike));
        if (!clause.linked.isEmpty()) {
            for (OnClause inner : clause.linked) {
                builder.append(inner.or ? OR : AND)
                        .append(String.format(ALIAS_COLUMN, this.parent.from, inner.joinedColumn)).append(evaluateOperation(inner, caseInsensitiveLike));
            }
        }
    }

    private String evaluateOperation(OnClause clause, boolean caseInsensitiveLike) {
        switch (clause.operation) {
            case EQUALS:
            case NOT_EQUALS:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_EQUALS:
            case GREATER_EQUALS:
                if (clause.value == null) {
                    return clause.operation.getValue() + String.format(ALIAS_COLUMN, this.joinTable, clause.joinColumn);
                } else {
                    return clause.operation.getValue() + "?";
                }
            case IS_NULL:
                return " IS NULL";
            case IS_NOT_NULL:
                return " IS NOT NULL";
            case IN:
            case NOT_IN:
                String columns;
                if (clause.iterable != null) {
                    columns = String.join(",", Collections.nCopies(getSize(clause.iterable), "?"));
                } else {
                    columns = clause.columns
                            .stream()
                            .map(s -> String.format(ALIAS_COLUMN, this.joinTable, s))
                            .collect(Collectors.joining(","));
                }
                return String.format(" %s (%s)", Operation.IN.equals(clause.operation) ? "IN" : "NOT IN", columns);
            case NOT_LIKE:
                return " NOT LIKE" + (clause.joinColumn != null ? clause.likeType.format(this.joinTable, clause.joinColumn, caseInsensitiveLike) : clause.likeType.getValue(caseInsensitiveLike));
            case LIKE:
                return " LIKE" + (clause.joinColumn != null ? clause.likeType.format(this.joinTable, clause.joinColumn, caseInsensitiveLike) : clause.likeType.getValue(caseInsensitiveLike));
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

    private List<SqlParameter> getParameters(List<OnClause> clauses) {
        List<SqlParameter> parameters = new ArrayList<>();
        for (OnClause clause : clauses) {
            if (clause.iterable != null) {
                parameters.addAll(StreamSupport.stream(clause.iterable.spliterator(), false)
                        .map(SqlParameter::new)
                        .collect(Collectors.toList()));
            } else if (clause.value != null) {
                parameters.add(new SqlParameter(clause.value));
            }
            if (!clause.linked.isEmpty()) {
                parameters.addAll(getParameters(clause.linked));
            }
        }

        return parameters;
    }

    static class OnClause {

        private final boolean or;
        private final ValueConverter<?, ?> converter;
        private Iterable<?> iterable;
        private LikeType likeType;
        private List<String> columns;
        private Object value;
        private final String joinedColumn;
        private final List<JoinImpl.OnClause> linked;
        private Operation operation;
        private String joinColumn;

        private OnClause(SqlColumn<?, ?> joinedColumn) {
            this(joinedColumn, false);
        }

        private OnClause(SqlColumn<?, ?> joinedColumn, boolean or) {
            this.joinedColumn = joinedColumn.getName();
            this.converter = joinedColumn.getConverter();
            this.linked = new ArrayList<>();
            this.or = or;
        }
    }
}
