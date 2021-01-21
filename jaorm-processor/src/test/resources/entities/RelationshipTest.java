package io.test;

import io.jaorm.processor.annotation.Column;
import io.jaorm.processor.annotation.Table;
@Table(name = "REL")
public class RelationshipTest {

    @Column(name = "TESTREL")
    private String test;

    @Column(name = "TESTREL2")
    private String test2;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }


    public String getTest2() {
        return test2;
    }

    public void setTest2(String test2) {
        this.test = test2;
    }
}