package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithSubQuerySupport;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public class SubQueryImpl<T, R> extends SelectedImpl<T, R> implements WithSubQuerySupport {

    private final String colum;

    @SuppressWarnings("unchecked")
    public SubQueryImpl(SqlColumn<?, R> column) {
        super((Class<T>) column.getEntity(), false);
        this.colum = column.getName();
    }

    @Override
    public String getSql() {
        StringBuilder builder = new StringBuilder("SELECT ")
                .append(this.table)
                .append(".")
                .append(colum)
                .append(" FROM ")
                .append(this.table);
        return buildExtraSql(builder);
    }
}
