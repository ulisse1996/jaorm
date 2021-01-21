import io.jaorm.processor.annotation.Column;
import io.jaorm.processor.annotation.Table;

@Table(name = "TABLE")
public class SimpleEntityNoGetter {

    @Column(name = "COL1")
    private String col1;

    @Column(name = "COL2")
    private String col2;
}