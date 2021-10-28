package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.annotation.Projection;
import io.github.ulisse1996.jaorm.entity.converter.BooleanIntConverter;

import java.math.BigDecimal;

@Projection
public class ProjectionWithConverter {

    @Column(name = "COL2")
    private String col2;

    @Column(name = "COL3")
    private BigDecimal col3;

    @Column(name = "COL1")
    @Converter(BooleanIntConverter.class)
    private boolean col1;

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public BigDecimal getCol3() {
        return col3;
    }

    public void setCol3(BigDecimal col3) {
        this.col3 = col3;
    }

    public boolean isCol1() {
        return col1;
    }

    public void setCol1(boolean col1) {
        this.col1 = col1;
    }
}
