package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Table(name = "COMPOUND_ENTITY")
@Data
public class CompoundEntity extends AbstractEntity {

    @Id
    @Column(name = "COMPOUND_ID")
    private int compoundId;
}
