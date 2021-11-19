package io.github.ulisse1996.jaorm.tools.service.sql;

public class TableAliasPair {

    private final String alias;
    private final String name;

    public TableAliasPair(String alias, String name) {
        this.alias = alias;
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TableAliasPair{" +
                "alias='" + alias + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public boolean hasAlias() {
        return alias != null && !alias.isEmpty();
    }
}
