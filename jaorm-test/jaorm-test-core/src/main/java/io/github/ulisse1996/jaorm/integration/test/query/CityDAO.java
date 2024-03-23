package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.integration.test.entity.City;
import io.github.ulisse1996.jaorm.specialization.SingleKeyDao;

import java.util.stream.Stream;

@Dao
public interface CityDAO extends SingleKeyDao<City, Integer> {

    @Query(sql = "SELECT * FROM CITY WHERE CITY_ID > ?")
    Stream<City> getCityHigherThanId(Integer id);
}
