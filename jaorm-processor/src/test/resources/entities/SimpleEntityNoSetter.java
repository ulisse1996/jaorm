import io.jaorm.processor.annotation.Column;
import io.jaorm.processor.annotation.Table;

@Table(name = "TABLE")
public class SimpleEntityNoSetter {

    @Column(name = "COL1")
    private String col1;

    @Column(name = "COL2")
    private String col2;

    public String getCol1() {
        return col1;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }
}