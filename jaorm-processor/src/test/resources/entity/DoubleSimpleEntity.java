package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

import java.util.Objects;

@Table(name = "TABLE")
public class DoubleSimpleEntity {

    @Id
    @Column(name = "COL1")
    private String col1;

    @Id
    @Column(name = "COL1_D")
    private long col1D;

    @Column(name = "COL2")
    private String col2;

    public long getCol1D() {
        return col1D;
    }

    public void setCol1D(long col1D) {
        this.col1D = col1D;
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
}
