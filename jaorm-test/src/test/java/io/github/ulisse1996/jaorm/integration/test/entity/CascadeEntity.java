package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.entity.Result;

@Table(name = "CASCADE_ENTITY")
public class CascadeEntity {

    @Id
    @Column(name = "CASCADE_ID")
    private int cascadeId;

    @Column(name = "CASCADE_NAME")
    private String name;

    @Cascade(CascadeType.ALL)
    @Relationship(columns = @Relationship.RelationshipColumn(targetColumn = "CASCADE_ID", sourceColumn = "CASCADE_ID"))
    private Result<CascadeEntityInner> cascadeInnerEntity;

    public Result<CascadeEntityInner> getCascadeInnerEntity() {
        return cascadeInnerEntity;
    }

    public void setCascadeInnerEntity(Result<CascadeEntityInner> cascadeInnerEntity) {
        this.cascadeInnerEntity = cascadeInnerEntity;
    }

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
