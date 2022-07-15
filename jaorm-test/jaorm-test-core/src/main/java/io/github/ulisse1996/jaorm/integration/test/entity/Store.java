package io.github.ulisse1996.jaorm.integration.test.entity;

import io.github.ulisse1996.jaorm.annotation.*;

import java.util.List;
import java.util.Objects;

@Table(name = "STORE")
public class Store {

    @Id
    @Column(name = "STORE_ID")
    private int storeId;

    @Column(name = "STORE_NAME")
    private String name;

    @Column(name = "CITY_ID")
    private int cityId;

    @Cascade(CascadeType.ALL)
    @Relationship(columns = @Relationship.RelationshipColumn(targetColumn = "STORE_ID", sourceColumn = "STORE_ID"))
    private List<Seller> sellers;

    public List<Seller> getSellers() {
        return sellers;
    }

    public void setSellers(List<Seller> sellers) {
        this.sellers = sellers;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return storeId == store.storeId && cityId == store.cityId && Objects.equals(name, store.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, name, cityId);
    }
}
