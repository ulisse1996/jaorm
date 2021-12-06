package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

@Table(name = "G_REL")
public class GEntityRel {

    @Id
    @Column(name = "COL1")
    private long col1;

    @Column(name = "COL4")
    private String col4;

    public long getCol1() {
        return col1;
    }

    public void setCol1(long col1) {
        this.col1 = col1;
    }

    public String getCol4() {
        return col4;
    }

    public void setCol4(String col4) {
        this.col4 = col4;
    }
}
