package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Relationship;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.entity.converter.ParameterConverter;

@Table(name = "TABLE")
public class EntityWithRelationship {

    @Id
    @Column(name = "COL1")
    private String col1;

    @Relationship(
            columns = @Relationship.RelationshipColumn(targetColumn = "COL_REL_1", defaultValue = "1", converter = ParameterConverter.BIG_DECIMAL)
    )
    private RelEntity relEntity;

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public RelEntity getRelEntity() {
        return relEntity;
    }

    public void setRelEntity(RelEntity relEntity) {
        this.relEntity = relEntity;
    }
}
