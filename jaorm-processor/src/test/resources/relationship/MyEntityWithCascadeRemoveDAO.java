package io.test;

import io.jaorm.BaseDao;
import io.jaorm.processor.annotation.Dao;

@Dao
public interface MyEntityWithCascadeRemoveDAO extends BaseDao<MyEntityWithCascadeRemove> {}