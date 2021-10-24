package io.test;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Table(name = "TABLE")
public class ComplexEntity {

    @Id
    @Column(name = "COL1")
    private boolean col1;

    @Column(name = "COL2")
    private String col2;

    @Column(name = "COL3")
    private short col3;

    @Column(name = "COL4")
    private int col4;

    @Column(name = "COL5")
    private long col5;

    @Column(name = "COL6")
    private float col6;

    @Column(name = "COL7")
    private double col7;

    @Column(name = "COL8")
    private BigDecimal col8;

    @Column(name = "COL9")
    private char col9;

    @Column(name = "COL10")
    private Date col10;

    @Column(name = "COL11")
    private Time col11;

    @Column(name = "COL12")
    private byte[] col12;

    @Column(name = "COL13")
    private BigDecimal col13;

    @Column(name = "COL14")
    private Timestamp col14;

    public BigDecimal getCol13() {
        return col13;
    }

    public void setCol13(BigDecimal col13) {
        this.col13 = col13;
    }

    public Timestamp getCol14() {
        return col14;
    }

    public void setCol14(Timestamp col14) {
        this.col14 = col14;
    }

    public boolean isCol1() {
        return col1;
    }

    public short getCol3() {
        return col3;
    }

    public void setCol3(short col3) {
        this.col3 = col3;
    }

    public int getCol4() {
        return col4;
    }

    public void setCol4(int col4) {
        this.col4 = col4;
    }

    public long getCol5() {
        return col5;
    }

    public void setCol5(long col5) {
        this.col5 = col5;
    }

    public float getCol6() {
        return col6;
    }

    public void setCol6(float col6) {
        this.col6 = col6;
    }

    public double getCol7() {
        return col7;
    }

    public void setCol7(double col7) {
        this.col7 = col7;
    }

    public BigDecimal getCol8() {
        return col8;
    }

    public void setCol8(BigDecimal col8) {
        this.col8 = col8;
    }

    public char getCol9() {
        return col9;
    }

    public void setCol9(char col9) {
        this.col9 = col9;
    }

    public Date getCol10() {
        return col10;
    }

    public void setCol10(Date col10) {
        this.col10 = col10;
    }

    public Time getCol11() {
        return col11;
    }

    public void setCol11(Time col11) {
        this.col11 = col11;
    }

    public byte[] getCol12() {
        return col12;
    }

    public void setCol12(byte[] col12) {
        this.col12 = col12;
    }

    public String getCol2() {
        return col2;
    }

    public boolean getCol1() {
        return col1;
    }

    public void setCol1(boolean col1) {
        this.col1 = col1;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleEntity that = (SimpleEntity) o;
        return Objects.equals(col1, that.col1) && Objects.equals(col2, that.col2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(col1, col2);
    }
}
