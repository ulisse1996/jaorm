package io.test;

import io.github.ulisse1996.jaorm.annotation.*;

@Table(name = "TABLE")
public class MyEntityWithCascadeUpdate {

    @Id
    @Column(name = "COL1")
    private String col1;

    @Column(name = "COL2")
    private String col2;

    @Column(name = "COL3")
    private String col3;

    @Cascade(CascadeType.UPDATE)
    @Relationship(
            columns = @Relationship.RelationshipColumn(sourceColumn = "COL3", targetColumn = "COL_1")
    )
    private MyRelEntity relEntity;

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public String getCol3() {
        return col3;
    }

    public void setCol3(String col3) {
        this.col3 = col3;
    }

    public MyRelEntity getRelEntity() {
        return relEntity;
    }

    public void setRelEntity(MyRelEntity relEntity) {
        this.relEntity = relEntity;
    }
}
