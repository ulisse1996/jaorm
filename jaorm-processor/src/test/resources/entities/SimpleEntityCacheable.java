package io.test;

import io.jaorm.annotation.Cacheable;
import io.jaorm.annotation.Column;
import io.jaorm.annotation.Id;
import io.jaorm.annotation.Table;

@Table(name = "ENTITY")
@Cacheable
public class SimpleEntityCacheable {

    @Id
    @Column(name = "COL1")
    private int col1;

    @Column(name = "COL2")
    private String col2;

    public int getCol1() {
        return col1;
    }

    public void setCol1(int col1) {
        this.col1 = col1;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }
}