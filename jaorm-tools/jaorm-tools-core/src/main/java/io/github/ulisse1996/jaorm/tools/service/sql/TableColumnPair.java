package io.github.ulisse1996.jaorm.tools.service.sql;

public class TableColumnPair {

    private final String table;
    private final String column;

    public TableColumnPair(String table, String column) {
        this.table = table;
        this.column = column;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "TableColumnPair{" +
                "table='" + table + '\'' +
                ", column='" + column + '\'' +
                '}';
    }
}
