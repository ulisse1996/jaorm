package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.*;

import java.util.List;
import java.util.Objects;

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

    private void doNothing() {
        // Test for private methods
    }

    protected void doNothingProtected() {
        // Test for protected methods
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return cityId == city.cityId && Objects.equals(name, city.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityId, name);
    }
}
