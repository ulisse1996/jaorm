package io.github.ulisse1996.jaorm.integration.test.lombok;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;
import lombok.Data;

@Table(name = "LOMBOK")
@Data
public class LombokEntity {

    @Id
    @Column(name = "LOMBOK_ID")
    private int lombokId;

    @Column(name = "LOMBOK_NAME")
    private String lombokName;

    @Column(name = "LOMBOK_BOOLEAN")
    private boolean lombokBoolean;
}
