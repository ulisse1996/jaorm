package io.jaorm.integration.test.query;

import io.jaorm.BaseDao;
import io.jaorm.integration.test.entity.City;
import io.jaorm.processor.annotation.Dao;

@Dao
public interface CityDAO extends BaseDao<City> {}
