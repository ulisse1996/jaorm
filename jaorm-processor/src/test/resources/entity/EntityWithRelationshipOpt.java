package io.test;

import io.jaorm.annotation.Column;
import io.jaorm.annotation.Id;
import io.jaorm.annotation.Relationship;
import io.jaorm.annotation.Table;
import io.jaorm.entity.converter.ParameterConverter;

import java.util.Optional;

@Table(name = "TABLE")
public class EntityWithRelationshipOpt {

    @Id
    @Column(name = "COL1")
    private String col1;

    @Relationship(
            columns = {
                        @Relationship.RelationshipColumn(targetColumn = "COL_REL_1", sourceColumn = "COL1"),
                        @Relationship.RelationshipColumn(targetColumn = "COL_REL_2", defaultValue = "2")
                    }
    )
    private Optional<RelEntity> relEntity;

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public Optional<RelEntity> getRelEntity() {
        return relEntity;
    }

    public void setRelEntity(Optional<RelEntity> relEntity) {
        this.relEntity = relEntity;
    }
}