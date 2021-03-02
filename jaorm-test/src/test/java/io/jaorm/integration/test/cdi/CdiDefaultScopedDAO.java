package io.jaorm.integration.test.cdi;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;

@Dao
public interface CdiDefaultScopedDAO extends BaseDao<CDIEntity> {
}
