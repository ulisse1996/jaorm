package io.test;

import io.jaorm.annotation.Column;
import io.jaorm.annotation.Relationship;
import io.jaorm.annotation.Table;

@Table(name = "TABLE")
public class SimpleEntityWithRelationshipAndMissingCurrentColumn {

    @Column(name = "COL1")
    private String col1;

    @Column(name = "COL2")
    private String col2;

    @Relationship(columns = @Relationship.RelationshipColumn(targetColumn = "COL"))
    private String col3;

    public String getCol3() {
        return col3;
    }

    public void setCol3(String col3) {
        this.col3 = col3;
    }

    public String getCol2() {
        return col2;
    }

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    @Table(name = "REL")
    public static class RelationshipTest {

        @Column(name = "TESTREL")
        private String test;

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }
    }
}