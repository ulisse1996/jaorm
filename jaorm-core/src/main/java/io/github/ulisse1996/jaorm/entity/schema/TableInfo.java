package io.github.ulisse1996.jaorm.entity.schema;

public class TableInfo {

    private final String table;
    private final String schema;

    public TableInfo(String table, String schema) {
        this.table = table;
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public String getSchema() {
        return schema;
    }
}
