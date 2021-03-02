import io.jaorm.annotation.Column;
import io.jaorm.annotation.Table;

@Table(name = "TABLE")
public class SimpleEntityNoGetter {

    @Column(name = "COL1")
    private String col1;

    @Column(name = "COL2")
    private String col2;
}