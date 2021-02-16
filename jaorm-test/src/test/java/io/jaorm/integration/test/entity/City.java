package io.jaorm.integration.test.entity;

import io.jaorm.processor.annotation.CascadeType;
import io.jaorm.processor.annotation.*;

import java.util.List;

@Table(name = "CITY")
public class City {

    @Id
    @Column(name = "CITY_ID")
    private int cityId;

    @Column(name = "CITY_NAME")
    private String name;

    @Cascade(CascadeType.ALL)
    @Relationship(columns = @Relationship.RelationshipColumn(targetColumn = "CITY_ID", sourceColumn = "CITY_ID"))
    private List<Store> stores;

    public List<Store> getStores() {
        return stores;
    }

    public void setStores(List<Store> stores) {
        this.stores = stores;
    }

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
