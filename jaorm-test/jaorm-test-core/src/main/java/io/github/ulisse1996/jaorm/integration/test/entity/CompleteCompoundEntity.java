package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Table(name = "COMPOUND_ENTITY_COMPLETE")
@Data
public class CompleteCompoundEntity extends CompoundEntity {

    @Column(name = "COMPLETE_ID")
    private int completeId;
}
