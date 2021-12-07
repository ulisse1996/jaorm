package io.test;

import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.entity.Result;

@Table(name = "G_ENTITY_ALL")
@Graph.Graphs(value = {
        @Graph(name = "AllV", nodes = "rels"),
        @Graph(name = "AllV2", nodes = "rels2")
})
public class GEntityAll {

    @Id
    @Column(name = "COL1")
    private long col1;

    @Column(name = "COL2")
    private String col2;

    @Relationship(columns = @Relationship.RelationshipColumn(targetColumn = "COL1", sourceColumn = "COL1"))
    private Result<GEntityRel> rels;

    @Relationship(columns = {
            @Relationship.RelationshipColumn(targetColumn = "COL1", sourceColumn = "COL1"),
            @Relationship.RelationshipColumn(targetColumn = "COL4", defaultValue = "4")
    })
    private GEntityRel rels2;

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

    public Result<GEntityRel> getRels() {
        return rels;
    }

    public void setRels(Result<GEntityRel> rels) {
        this.rels = rels;
    }

    public GEntityRel getRels2() {
        return rels2;
    }

    public void setRels2(GEntityRel rels2) {
        this.rels2 = rels2;
    }
}
