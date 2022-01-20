package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Table(name = "ENTITY_WITH_DEFAULTS")
public class EntityWithDefaults {

    @Id
    @Column(name = "E_ID")
    private BigInteger id;

    @Column(name = "E_STR")
    @DefaultString("STRING")
    private String str;

    @Column(name = "E_NUM")
    @DefaultNumeric(0.390)
    private BigDecimal dec;

    @Column(name = "E_DATE")
    @DefaultTemporal
    private Date date;

    @Column(name = "E_DATE_FORMAT")
    @DefaultTemporal(format = "dd-MM-yyyy'T'HH:mm:ss", value = "20-10-2022T00:00:00")
    private Date dateWithFormat;

    public Date getDateWithFormat() {
        return dateWithFormat;
    }

    public void setDateWithFormat(Date dateWithFormat) {
        this.dateWithFormat = dateWithFormat;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public BigDecimal getDec() {
        return dec;
    }

    public void setDec(BigDecimal dec) {
        this.dec = dec;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
