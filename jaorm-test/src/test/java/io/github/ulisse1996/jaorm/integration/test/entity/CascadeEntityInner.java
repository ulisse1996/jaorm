package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

@Table(name = "CASCADE_ENTITY_INNER")
public class CascadeEntityInner {

    @Id
    @Column(name = "CASCADE_ID")
    private int cascadeId;

    @Column(name = "CASCADE_INNER_NAME")
    private String name;

    public int getCascadeId() {
        return cascadeId;
    }

    public void setCascadeId(int cascadeId) {
        this.cascadeId = cascadeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
