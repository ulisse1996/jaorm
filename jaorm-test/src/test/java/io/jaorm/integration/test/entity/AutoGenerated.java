package io.jaorm.integration.test.entity;

import io.jaorm.annotation.Column;
import io.jaorm.annotation.Id;
import io.jaorm.annotation.Table;

import java.math.BigDecimal;

@Table(name = "AUTO_GEN")
public class AutoGenerated {

    @Id(autoGenerated = true)
    @Column(name = "GEN_ID")
    private BigDecimal colGen;

    @Column(name = "NAME")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getColGen() {
        return colGen;
    }

    public void setColGen(BigDecimal colGen) {
        this.colGen = colGen;
    }
}
