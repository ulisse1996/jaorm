package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.config.WhereChecker;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithSubQuerySupport;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractWhereImpl<T, R> {

    private static final String WHERE = " WHERE ";
    protected static final String AND_CLAUSE = " AND ";
    protected static final String OR_CLAUSE = " OR ";

    protected final SqlColumn<?, R> column;
    protected final boolean or;
    protected final List<AbstractWhereImpl<T, ?>> links;
    protected boolean valid;
    protected Operation op;
    private R value;
    protected LikeType likeType;
    private final String alias;
    protected Iterable<R> iterable;
    protected WithSubQuerySupport subQuery;

    protected AbstractWhereImpl(SqlColumn<?, R> column, boolean or, String alias) {
        this.column = column;
        this.or = or;
        this.links = new ArrayList<>();
        this.valid = true;
        this.alias = alias;
    }

    protected void assertIsString() {
        if (column == null) {
            WhereFunctionImpl<?, ?> functionWhere = (WhereFunctionImpl<?, ?>) this;
            functionWhere.assertIsString(functionWhere.getFunction());
        } else {
            if (!column.getType().equals(String.class)) {
                throw new IllegalArgumentException("Can't use like without a column that match String.class");
            }
        }
    }

    public void addLinked(AbstractWhereImpl<T, ?> where) {
        this.links.add(where);
    }

    protected <M> M operation(R value, Operation operation, M parent) {
        this.valid = this.getChecker().isValidWhere(this.column, operation, value);
        this.op = operation;
        this.value = value;
        return parent;
    }

    public String asString(boolean first, boolean caseInsensitiveLike) {
        if (!valid) {
            return "";
        }
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
        String val;
        if (this.column != null) {
            val = String.format(format, getFrom(this), this.column.getName());
        } else {
            val = getFunctionFormat(caseInsensitiveLike);
        }
        builder.append(val).append(evaluateOperation(this, caseInsensitiveLike));
        buildLinked(builder, caseInsensitiveLike);
    }

    private String getFunctionFormat(boolean caseInsensitiveLike) {
        WhereFunctionImpl<?, ?> func = (WhereFunctionImpl<?, ?>) this;
        return func.getFormat(func.getFunction(), caseInsensitiveLike, this);
    }

    protected void buildLinked(StringBuilder builder, boolean caseInsensitiveLike) {
        if (!this.links.isEmpty()) {
            for (AbstractWhereImpl<?, ?> inner : this.links) {
                buildInner(builder, caseInsensitiveLike, inner);
            }
        }
    }

    protected void buildInner(StringBuilder builder, boolean caseInsensitiveLike, AbstractWhereImpl<?, ?> inner) {
        String innerFormat = caseInsensitiveLike && inner.likeType != null ? "UPPER(%s.%s)" : "%s.%s";
        String format;
        if (inner.column != null) {
            format = String.format(innerFormat, getFrom(inner), inner.column.getName());
        } else {
            WhereFunctionImpl<?, ?> func = (WhereFunctionImpl<?, ?>) inner;
            format = func.getFormat(func.getFunction(), caseInsensitiveLike, inner);
        }
        builder.append(inner.or ? OR_CLAUSE : AND_CLAUSE)
                .append(format).append(evaluateOperation(inner, caseInsensitiveLike));
    }

    protected String evaluateOperation(AbstractWhereImpl<?, ?> clause, boolean caseInsensitiveLike) {
        switch (clause.op) {
            case EQUALS:
            case NOT_EQUALS:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_EQUALS:
            case GREATER_EQUALS:
                return clause.op.getValue() + "?";
            case IS_NULL:
                return " IS NULL";
            case IS_NOT_NULL:
                return " IS NOT NULL";
            case IN:
            case NOT_IN:
                if (clause.subQuery != null) {
                    return String.format(" %s (%s)", Operation.IN.equals(clause.op) ? "IN" : "NOT IN", this.subQuery.getSql());
                } else {
                    String wildcards = String.join(",", Collections.nCopies(getSize(clause.iterable), "?"));
                    return String.format(" %s (%s)", Operation.IN.equals(clause.op) ? "IN" : "NOT IN", wildcards);
                }
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

    protected WithSubQuerySupport assertIsSubQuery(Object obj) {
        if (!(obj instanceof WithSubQuerySupport)) {
            throw new IllegalArgumentException("Can't use Sub Query that is not instance of WithSubQuerySupport ! Please use QueryBuilder.subQuery Method");
        }

        return ((WithSubQuerySupport) obj);
    }

    protected String getFrom(AbstractWhereImpl<?, ?> where) {
        if (where.alias == null || where.alias.isEmpty()) {
            return where.getTable();
        } else {
            return where.alias;
        }
    }

    public Stream<SqlParameter> getParameters() {
        if (!valid) {
            return Stream.empty();
        }
        List<SqlParameter> parameters = new ArrayList<>();
        if (value != null) {
            parameters.add(new SqlParameter(value));
        } else if (iterable != null) {
            parameters.addAll(StreamSupport.stream(iterable.spliterator(), false).map(SqlParameter::new).collect(Collectors.toList()));
        } else {
            parameters.addAll(this.subQuery.getParameters());
        }
        return Stream.concat(
                parameters.stream(),
                links.stream().flatMap(AbstractWhereImpl::getParameters)
        );
    }

    protected abstract WhereChecker getChecker();
    protected abstract String getTable();
}
