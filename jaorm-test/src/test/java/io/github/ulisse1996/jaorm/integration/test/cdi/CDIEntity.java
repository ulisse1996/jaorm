package io.github.ulisse1996.jaorm.integration.test.cdi;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

import java.util.Objects;

@Table(name = "WELD")
public class CDIEntity {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CDIEntity entity = (CDIEntity) o;
        return Objects.equals(col1, entity.col1) && Objects.equals(col2, entity.col2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(col1, col2);
    }
}
