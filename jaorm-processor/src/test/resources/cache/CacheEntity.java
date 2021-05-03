package io.test;

import io.github.ulisse1996.jaorm.annotation.Cacheable;
import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

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
