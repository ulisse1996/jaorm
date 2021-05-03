package io.test;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.test.SimpleEntity;

@Dao
public interface QueryWithBaseDaoWithoutCustomMethods extends BaseDao<io.test.SimpleEntity> {}
