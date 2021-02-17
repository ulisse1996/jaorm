package io.test;

import io.jaorm.BaseDao;
import io.jaorm.processor.annotation.Dao;

@Dao
public interface MyEntityWithCascadePersistDAO extends BaseDao<MyEntityWithCascadePersist> {}