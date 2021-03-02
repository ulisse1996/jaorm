package io.test;

import io.jaorm.annotation.Column;
import io.jaorm.annotation.Converter;
import io.jaorm.annotation.Id;
import io.jaorm.annotation.Table;
import io.jaorm.entity.converter.BooleanStringConverter;

import java.util.Objects;

@Table(name = "TABLE")
public class SimpleEntity {

    @Id
    @Column(name = "COL1")
    private String col1;

    @Column(name = "COL2")
    private String col2;

    @Column(name = "COL3")
    private int col3;

    @Column(name = "COL4")
    @Converter(BooleanStringConverter.class)
    private boolean col4;

    public int getCol3() {
        return col3;
    }

    public void setCol3(int col3) {
        this.col3 = col3;
    }

    public boolean isCol4() {
        return col4;
    }

    public void setCol4(boolean col4) {
        this.col4 = col4;
    }

    public String getCol2() {
        return col2;
    }

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleEntity that = (SimpleEntity) o;
        return Objects.equals(col1, that.col1) && Objects.equals(col2, that.col2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(col1, col2);
    }
}