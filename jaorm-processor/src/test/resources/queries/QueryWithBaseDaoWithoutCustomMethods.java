package io.test;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;
import io.test.SimpleEntity;

@Dao
public interface QueryWithBaseDaoWithoutCustomMethods extends BaseDao<io.test.SimpleEntity> {}