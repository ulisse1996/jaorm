package io.github.ulisse1996.jaorm.integration.test.global;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.GlobalListener;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;

@GlobalListener
@Table(name = "CITY")
public class GlobalEntityCity {

    @Id
    @Column(name = "CITY_ID")
    private int cityId;

    @Column(name = "CITY_NAME")
    private String name;

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
