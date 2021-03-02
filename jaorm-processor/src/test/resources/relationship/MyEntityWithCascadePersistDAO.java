package io.test;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;

@Dao
public interface MyEntityWithCascadePersistDAO extends BaseDao<MyEntityWithCascadePersist> {}