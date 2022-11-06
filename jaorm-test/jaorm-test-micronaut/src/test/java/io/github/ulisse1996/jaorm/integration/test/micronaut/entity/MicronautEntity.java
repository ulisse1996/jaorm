package io.github.ulisse1996.jaorm.integration.test.micronaut.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

import java.math.BigDecimal;

@Table(name = "MICRONAUT_ENTITY")
public class MicronautEntity {

    @Id
    @Column(name = "COL1")
    private String entityId;

    @Column(name = "COL2")
    private String col2;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }
}
