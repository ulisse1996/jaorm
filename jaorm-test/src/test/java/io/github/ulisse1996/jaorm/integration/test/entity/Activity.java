package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.entity.EntityRecord;

import java.math.BigDecimal;
import java.util.Date;

@Table(name = "ACTIVITY")
public class Activity implements EntityRecord<Activity> {

    @Id
    @Column(name = "ACTIVITY_ID")
    private BigDecimal id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ACTIVITY_DATE")
    private Date date;

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
