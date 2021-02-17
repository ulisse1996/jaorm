package io.jaorm.integration.test.query;

import io.jaorm.BaseDao;
import io.jaorm.integration.test.entity.Store;
import io.jaorm.processor.annotation.Dao;

@Dao
public interface StoreDAO extends BaseDao<Store> {}
