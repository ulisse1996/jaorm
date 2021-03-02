package io.test;

import io.jaorm.entity.converter.BooleanStringConverter;
import io.jaorm.annotation.Column;
import io.jaorm.annotation.Converter;
import io.jaorm.annotation.Table;

@Table(name = "TABLE")
public class SimpleEntityWithConverter {

    @Column(name = "COL1")
    private String col1;

    @Column(name = "COL2")
    private String col2;

    @Converter(value = BooleanStringConverter.class)
    @Column(name = "COL3")
    private boolean testBool;

    public boolean isTestBool() {
        return testBool;
    }

    public void setTestBool(boolean testBool) {
        this.testBool = testBool;
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
}