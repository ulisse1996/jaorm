package io.test;

import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;

@Table(name = "TABLE")
public class EntityWithRelationshipOpt {

    @Id
    @Column(name = "COL1")
    @Converter(EmptyConverter.class)
    private String col1;

    @Relationship(
            columns = {
                        @Relationship.RelationshipColumn(targetColumn = "COL_REL_1", sourceColumn = "COL1"),
                        @Relationship.RelationshipColumn(targetColumn = "COL_REL_2", defaultValue = "2")
                    }
    )
    private Result<RelEntity> relEntity;

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public Result<RelEntity> getRelEntity() {
        return relEntity;
    }

    public void setRelEntity(Result<RelEntity> relEntity) {
        this.relEntity = relEntity;
    }

    public static class EmptyConverter implements ValueConverter<String, String> {

        @Override
        public String toSql(String val) {
            return val;
        }

        @Override
        public String fromSql(String val) {
            return val;
        }
    }
}
