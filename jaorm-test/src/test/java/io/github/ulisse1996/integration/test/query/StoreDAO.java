package io.github.ulisse1996.integration.test.query;

import io.github.ulisse1996.integration.test.entity.Store;
import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Dao;

@Dao
public interface StoreDAO extends BaseDao<Store> {}
