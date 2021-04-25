package io.jaorm.integration.test.entity;

import io.jaorm.annotation.Column;
import io.jaorm.annotation.Id;
import io.jaorm.annotation.Table;

@Table(name = "CUSTOM_ACCESSOR")
public class CustomAccessor {

    @Id
    @Column(name = "CUSTOM")
    private MyEnumCustom custom;

    public MyEnumCustom getCustom() {
        return custom;
    }

    public void setCustom(MyEnumCustom custom) {
        this.custom = custom;
    }

    public enum MyEnumCustom {
        VAL
    }
}
