package io.github.ulisse1996.integration.test.entity;

import io.github.ulisse1996.annotation.Column;
import io.github.ulisse1996.annotation.Id;
import io.github.ulisse1996.annotation.Table;

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
