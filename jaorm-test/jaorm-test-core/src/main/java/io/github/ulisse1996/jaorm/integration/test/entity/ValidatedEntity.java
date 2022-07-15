package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "VAL")
public class ValidatedEntity {

    @Id
    @Column(name = "VAL_ID_1")
    private BigDecimal val1;

    @Column(name = "VAL_COL_2")
    @NotEmpty
    private String val2;

    @Column(name = "VAL_DATE")
    @Future
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getVal1() {
        return val1;
    }

    public void setVal1(BigDecimal val1) {
        this.val1 = val1;
    }

    public String getVal2() {
        return val2;
    }

    public void setVal2(String val2) {
        this.val2 = val2;
    }
}
