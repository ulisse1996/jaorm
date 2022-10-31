package io.github.ulisse1996.jaorm.dsl.query.impl.simple;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.dsl.util.Checker;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

public class AliasColumn {

    private final SqlColumn<?, ?> column;
    private final String tableAlias;
    private final VendorFunction<?> function;

    public AliasColumn(Selectable<?> selectable, String alias) {
        Checker.assertNotNull(selectable, "selectable");
        if (selectable instanceof SqlColumn) {
            this.column = (SqlColumn<?, ?>) selectable;
            this.function = null;
        } else if (selectable instanceof VendorFunction){
            this.function = (VendorFunction<?>) selectable;
            this.column = null;
        } else {
            throw new IllegalArgumentException(String.format("Invalid type %s for selectable", selectable.getClass()));
        }
        this.tableAlias = alias;
    }

    public VendorFunction<?> getFunction() { //NOSONAR
        return function;
    }

    public SqlColumn<?, ?> getColumn() { //NOSONAR
        return column;
    }

    public String getTableAlias() {
        return tableAlias;
    }
}