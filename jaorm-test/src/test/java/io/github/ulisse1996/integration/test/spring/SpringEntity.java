package io.github.ulisse1996.integration.test.spring;

import io.github.ulisse1996.annotation.Column;
import io.github.ulisse1996.annotation.Id;
import io.github.ulisse1996.annotation.Table;

@Table(name = "SPRING")
public class SpringEntity {

    @Id
    @Column(name = "COL1")
    private String col1;

    @Column(name = "COL2")
    private String col2;

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }
}
