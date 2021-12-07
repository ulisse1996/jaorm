package io.test;

import io.github.ulisse1996.jaorm.annotation.*;

import java.util.List;

@Table(name = "G_ENTITY")
@Graph(name = "All", nodes = "rels")
public class GEntity {

    @Id
    @Column(name = "COL1")
    private long col1;

    @Column(name = "COL2")
    private String col2;

    @Relationship(columns = @Relationship.RelationshipColumn(targetColumn = "COL1", sourceColumn = "COL1"))
    private List<GEntityRel> rels;

    public long getCol1() {
        return col1;
    }

    public void setCol1(long col1) {
        this.col1 = col1;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public List<GEntityRel> getRels() {
        return rels;
    }

    public void setRels(List<GEntityRel> rels) {
        this.rels = rels;
    }
}
