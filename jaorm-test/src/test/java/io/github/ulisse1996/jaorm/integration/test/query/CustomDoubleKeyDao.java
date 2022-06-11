package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.integration.test.entity.DoubleIdEntity;
import io.github.ulisse1996.jaorm.specialization.DoubleKeyDao;

@Dao
public interface CustomDoubleKeyDao extends DoubleKeyDao<DoubleIdEntity, Long, Long> {
}

