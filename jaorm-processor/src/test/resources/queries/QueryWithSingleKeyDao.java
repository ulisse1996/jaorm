package io.test;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.specialization.SingleKeyDao;

@Dao
public interface QueryWithSingleKeyDao extends SingleKeyDao<io.test.SingleSimpleEntity, Integer> {}
