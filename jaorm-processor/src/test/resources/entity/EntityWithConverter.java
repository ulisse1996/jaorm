package io.test;

import io.jaorm.annotation.*;
import io.jaorm.entity.converter.BooleanIntConverter;
import io.jaorm.entity.converter.ParameterConverter;

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