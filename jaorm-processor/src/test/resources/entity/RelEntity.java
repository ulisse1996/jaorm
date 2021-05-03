package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

import java.math.BigDecimal;

@Table(name = "REL_ENTIY")
public class RelEntity {

    @Id
    @Column(name = "COL_REL_1")
    private BigDecimal colRel1;

    @Id
    @Column(name = "COL_REL_2")
    private String colRel2;

    public String getColRel2() {
        return colRel2;
    }

    public void setColRel2(String colRel2) {
        this.colRel2 = colRel2;
    }

    public BigDecimal getColRel1() {
        return colRel1;
    }

    public void setColRel1(BigDecimal colRel1) {
        this.colRel1 = colRel1;
    }
}
