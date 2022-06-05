package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithSubQuerySupport;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WhenImpl {

    private final SqlColumn<?, ?> column;
    private final String alias;
    private final boolean orElse;
    private Operation operation;
    private Object value;
    private List<Object> values;
    private ThenImpl<?> then;
    private WithSubQuerySupport subQuery;
    private LikeType likeType;

    public WhenImpl(SqlColumn<?, ?> column, boolean orElse) {
        this(column, null, orElse);
    }

    public WhenImpl(SqlColumn<?, ?> column, String alias, boolean orElse) {
        this.column = column;
        this.alias = alias;
        this.orElse = orElse;
    }

    public <R> void setValue(R value) {
        this.value = value;
    }

    public void setThen(SqlColumn<?, ?> column) {
        this.then = new ThenImpl<>(column);
    }

    public <R> void setThen(R value) {
        this.then = new ThenImpl<>(value);
    }

    public <R> void setThen(SqlColumn<?,R> column, String alias) {
        this.then = new ThenImpl<>(column, alias);
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public void setValues(Iterable<?> iterable) {
        this.values = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    public void setSubQuery(WithSubQuerySupport subQuery) {
        this.subQuery = subQuery;
    }

    public String asString(String table, boolean caseInsensitiveLike) {
        if (this.orElse) {
            if (this.value != null) {
                return "ELSE ?";
            } else {
                return String.format("ELSE %s", this.column != null ? formatColumn(this.alias, this.column, table) : "?");
            }
        } else {
            StringBuilder builder = new StringBuilder("WHEN ")
                    .append(formatColumn(this.alias, this.column, table));
            builder.append(appendOperation(caseInsensitiveLike));
            builder.append(" THEN ");
            if (then.value != null) {
                builder.append("?");
            } else {
                builder.append(formatColumn(then.alias, then.column, table));
            }

            return builder.toString();
        }
    }

    private String appendOperation(boolean caseInsensitiveLike) {
        StringBuilder builder = new StringBuilder();
        switch (operation) {
            case EQUALS:
            case NOT_EQUALS:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_EQUALS:
            case GREATER_EQUALS:
                builder.append(operation.getValue()).append("?");
                break;
            case IS_NULL:
                builder.append(" IS NULL");
                break;
            case IS_NOT_NULL:
                builder.append(" IS NOT NULL");
                break;
            case IN:
            case NOT_IN:
                if (this.subQuery != null) {
                    builder.append(String.format(" %s (%s)", Operation.IN.equals(operation) ? "IN" : "NOT IN", this.subQuery.getSql()));
                } else {
                    String wildcards = String.join(",", Collections.nCopies(this.values.size(), "?"));
                    builder.append(String.format(" %s (%s)", Operation.IN.equals(operation) ? "IN" : "NOT IN", wildcards));
                }
                break;
            case NOT_LIKE:
                builder.append(" NOT LIKE").append(likeType.getValue(caseInsensitiveLike));
                break;
            case LIKE:
                builder.append(" LIKE").append(likeType.getValue(caseInsensitiveLike));
                break;
            default:
                throw new IllegalArgumentException("Can't find operation type");
        }

        return builder.toString();
    }

    private String formatColumn(String alias, SqlColumn<?, ?> column, String table) {
        String a = alias != null ? alias : table;
        return String.format("%s.%s", a, column.getName());
    }

    public Collection<SqlParameter> getAllValues() {
        List<SqlParameter> params = new ArrayList<>();
        if (values != null) {
            params.addAll(values.stream().map(SqlParameter::new).collect(Collectors.toList()));
        } else if (value != null) {
            params.add(new SqlParameter(this.value));
        } else if (subQuery != null) {
            params.addAll(subQuery.getParameters());
        }

        if (this.then != null && this.then.value != null) {
            params.add(new SqlParameter(this.then.value));
        }

        return params;
    }

    public void setLikeType(LikeType type) {
        this.likeType = type;
    }

    private static class ThenImpl<R> {

        private String alias;
        private SqlColumn<?, ?> column;
        private R value;

        private ThenImpl(R value) {
            this.value = value;
        }

        private ThenImpl(SqlColumn<?, ?> column) {
            this(column, null);
        }

        private ThenImpl(SqlColumn<?, ?> column, String alias) {
            this.column = column;
            this.alias = alias;
        }
    }
}
