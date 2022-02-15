package io.test;

import io.github.ulisse1996.jaorm.annotation.*;

import java.math.BigDecimal;

@Table(name = "TABLE")
public class EntityWithCustomConverterAndRel {

    @Id
    @Converter(StringToBigDecimalConverter.class)
    @Column(name = "COL1")
    private BigDecimal col1;

    @Column(name = "COL2")
    @Converter(StringToBigDecimalConverter.class)
    private BigDecimal col2;

    @Cascade(CascadeType.ALL)
    @Relationship(
            columns = {
                    @Relationship.RelationshipColumn(targetColumn = "COL_REL_1", sourceColumn = "COL1"),
                    @Relationship.RelationshipColumn(targetColumn = "COL_REL_2", defaultValue = "NAME2"),
                    @Relationship.RelationshipColumn(targetColumn = "COL_REL_3", sourceColumn = "COL2")
            }
    )
    private RelEntityCustom relEntity;

    public BigDecimal getCol1() {
        return col1;
    }

    public void setCol1(BigDecimal col1) {
        this.col1 = col1;
    }

    public BigDecimal getCol2() {
        return col2;
    }

    public void setCol2(BigDecimal col2) {
        this.col2 = col2;
    }

    public RelEntityCustom getRelEntity() {
        return relEntity;
    }

    public void setRelEntity(RelEntityCustom relEntity) {
        this.relEntity = relEntity;
    }
}
