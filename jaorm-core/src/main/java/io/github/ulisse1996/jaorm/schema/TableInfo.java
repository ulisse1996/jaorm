package io.github.ulisse1996.jaorm.schema;

import io.github.ulisse1996.jaorm.annotation.Table;

public class TableInfo {

    public static final TableInfo EMPTY = new TableInfo("", Object.class, Table.UNSET);

    private final String table;
    private final Class<?> entity;
    private final String schema;

    public TableInfo(String table, Class<?> entity, String schema) {
        this.table = table;
        this.entity = entity;
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public Class<?> getEntity() {
        return entity;
    }

    public String getSchema() {
        return schema;
    }
}
