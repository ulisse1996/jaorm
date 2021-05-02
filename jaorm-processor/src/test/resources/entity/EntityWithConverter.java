package io.test;

import io.github.ulisse1996.annotation.Column;
import io.github.ulisse1996.annotation.Converter;
import io.github.ulisse1996.annotation.Id;
import io.github.ulisse1996.annotation.Table;
import io.github.ulisse1996.entity.converter.BooleanIntConverter;

@Table(name = "TABLE")
public class EntityWithConverter {

    @Id
    @Column(name = "COL1")
    @Converter(BooleanIntConverter.class)
    private boolean col1;

    public boolean isCol1() {
        return col1;
    }

    public void setCol1(boolean col1) {
        this.col1 = col1;
    }
}
