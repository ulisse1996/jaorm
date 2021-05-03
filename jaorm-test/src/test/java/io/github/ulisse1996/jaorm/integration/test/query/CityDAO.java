package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.integration.test.entity.City;
import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;

@Dao
public interface CityDAO extends BaseDao<City> {}
