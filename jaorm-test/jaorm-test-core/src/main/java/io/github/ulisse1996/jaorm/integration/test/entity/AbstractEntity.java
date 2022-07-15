package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.entity.event.PrePersist;
import lombok.Data;

import java.util.Date;

@Data
public abstract class AbstractEntity implements PrePersist<RuntimeException> {

    @Column(name = "CREATE_DATE")
    protected Date createDate;

    @Column(name = "CREATE_USER")
    protected String creationUser;

    @Override
    public void prePersist() {
        this.createDate = new Date();
        this.creationUser = "1";
    }
}
