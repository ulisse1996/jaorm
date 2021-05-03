package io.test;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;

@Dao
public interface MyEntityWithCascadePersistDAO extends BaseDao<MyEntityWithCascadePersist> {}
