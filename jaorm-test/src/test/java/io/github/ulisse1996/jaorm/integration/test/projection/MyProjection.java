package io.github.ulisse1996.jaorm.integration.test.projection;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.annotation.Projection;
import io.github.ulisse1996.jaorm.entity.converter.BooleanIntConverter;

import java.math.BigDecimal;
import java.util.Date;

@Projection
public class MyProjection {

    @Column(name = "ID_COL")
    private BigDecimal id;

    @Column(name = "SUB_NAME")
    private String subName;

    @Column(name = "VALID")
    @Converter(BooleanIntConverter.class)
    private boolean valid;

    private Date other;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Date getOther() {
        return other;
    }

    public void setOther(Date other) {
        this.other = other;
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }
}
