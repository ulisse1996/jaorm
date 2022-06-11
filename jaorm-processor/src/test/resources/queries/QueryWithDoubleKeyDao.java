package io.test;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.specialization.DoubleKeyDao;

@Dao
public interface QueryWithDoubleKeyDao extends DoubleKeyDao<io.test.DoubleSimpleEntity, String, Long> {}
