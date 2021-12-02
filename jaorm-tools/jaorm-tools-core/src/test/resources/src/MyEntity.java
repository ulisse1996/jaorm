package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

@Table(name = "TAB")
public class MyEntity {

    @Id
    @Column(name = "COL1")
    private int col1;

    @Column(name = "COL2")
    private String col2;

    public int getCol1() {
        return col1;
    }

    public String getCol2() {
        return col2;
    }
}
