package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public class Sort<T> {

    private final SqlColumn<T, ?> column;
    private final String sortType;

    private Sort(SqlColumn<T, ?> column, String sortType) {
        this.column = column;
        this.sortType = sortType;
    }

    public static <R> Sort<R> asc(SqlColumn<R, ?> column) {
        return new Sort<>(column, "ASC");
    }

    public static <R> Sort<R> desc(SqlColumn<R, ?> column) {
        return new Sort<>(column, "DESC");
    }

    public SqlColumn<T, ?> getColumn() { //NOSONAR
        return column;
    }

    public String getSortType() {
        return sortType;
    }
}
