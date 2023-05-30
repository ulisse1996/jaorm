package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

import java.math.BigDecimal;

@Table(name = "REL_ENTIY_CUSTOM")
public class RelEntityCustom {

    @Id
    @Column(name = "COL_REL_1")
    private BigDecimal colRel1;

    @Column(name = "COL_REL_2")
    @Converter(EnumConverter.class)
    private CustomEnum colRel2;

    @Column(name = "COL_REL_3")
    @Converter(StringToBigDecimalConverter.class)
    private BigDecimal colRel3;

    public BigDecimal getColRel3() {
        return colRel3;
    }

    public void setColRel3(BigDecimal colRel3) {
        this.colRel3 = colRel3;
    }

    public CustomEnum getColRel2() {
        return colRel2;
    }

    public void setColRel2(CustomEnum colRel2) {
        this.colRel2 = colRel2;
    }

    public BigDecimal getColRel1() {
        return colRel1;
    }

    public void setColRel1(BigDecimal colRel1) {
        this.colRel1 = colRel1;
    }

}
