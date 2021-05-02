package io.test;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Dao;
import io.test.SimpleEntity;

@Dao
public interface QueryWithBaseDaoWithoutCustomMethods extends BaseDao<io.test.SimpleEntity> {}
