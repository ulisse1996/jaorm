package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Relationship;
import io.github.ulisse1996.jaorm.annotation.Table;

import java.util.List;

@Table(name = "TABLE")
public class EntityWithRelationshipCollection {

    @Id
    @Column(name = "COL1")
    private String col1;

    @Relationship(
            columns = {
                        @Relationship.RelationshipColumn(targetColumn = "COL_REL_1", sourceColumn = "COL1"),
                        @Relationship.RelationshipColumn(targetColumn = "COL_REL_2", defaultValue = "2")
                    }
    )
    private List<RelEntity> relEntity;

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public List<RelEntity> getRelEntity() {
        return relEntity;
    }

    public void setRelEntity(List<RelEntity> relEntity) {
        this.relEntity = relEntity;
    }
}
