package io.test;

import io.github.ulisse1996.annotation.Cacheable;
import io.github.ulisse1996.annotation.Column;
import io.github.ulisse1996.annotation.Id;
import io.github.ulisse1996.annotation.Table;

@Cacheable
@Table(name = "TABLE")
public class CacheEntity {

    @Id
    @Column(name = "COL1")
    private String col1;

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }
}
