package io.github.ulisse1996.jaorm.dsl.query.impl.simple;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSelectedImpl {

    protected List<AliasColumn> columns;

    protected AbstractSelectedImpl(List<AliasColumn> columns, SqlColumn<?, ?> column, String alias) {
        this.columns = Stream.concat(columns.stream(), Stream.of(new AliasColumn(column, alias)))
                .collect(Collectors.toList());
    }

    protected AbstractSelectedImpl(List<AliasColumn> columns, VendorFunction<?> function, String alias) {
        this.columns = Stream.concat(columns.stream(), Stream.of(new AliasColumn(function, alias)))
                .collect(Collectors.toList());
    }
}
