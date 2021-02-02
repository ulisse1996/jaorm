package io.test;

import io.jaorm.BaseDao;
import io.jaorm.processor.annotation.Dao;
import io.jaorm.processor.annotation.Query;
import io.test.SimpleEntity;

@Dao
public interface QueryWithBaseDaoWithoutCustomMethods extends BaseDao<io.test.SimpleEntity> {}