package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.DefaultNumeric;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Table(name = "TABLE")
public class EntityWithDefaultNumeric {

    @Id
    @Column(name = "COL1")
    private String col1;

    @Column(name = "COL2")
    private String col2;

    @Column(name = "COL3")
    @DefaultNumeric(0.0)
    private BigDecimal numeric;

    public BigDecimal getNumeric() {
        return numeric;
    }

    public void setNumeric(BigDecimal numeric) {
        this.numeric = numeric;
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
        EntityWithDefaultNumeric that = (EntityWithDefaultNumeric) o;
        return Objects.equals(col1, that.col1) && Objects.equals(col2, that.col2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(col1, col2);
    }
}
