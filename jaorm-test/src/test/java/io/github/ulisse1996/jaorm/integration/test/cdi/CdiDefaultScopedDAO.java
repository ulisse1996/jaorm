package io.github.ulisse1996.jaorm.integration.test.cdi;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;

@Dao
public interface CdiDefaultScopedDAO extends BaseDao<CDIEntity> {
}
