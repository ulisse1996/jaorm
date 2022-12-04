package io.github.ulisse1996.jaorm.integration.test.postgre.entity;

import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.spatial.Geography;

@Table(name = "POSTGIS_ENTITY")
public class PostgisEntity {

    @Column(name = "ID")
    @Id
    private Integer id;

    @Column(name = "GEOG")
    private Geography geography;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Geography getGeography() {
        return geography;
    }

    public void setGeography(Geography geography) {
        this.geography = geography;
    }
}
