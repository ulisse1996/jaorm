package io.github.ulisse1996.integration.test.cdi;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Dao;

@Dao
public interface CdiDefaultScopedDAO extends BaseDao<CDIEntity> {
}
