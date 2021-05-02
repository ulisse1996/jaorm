package io.github.ulisse1996.integration.test.cdi;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.integration.test.inject.MyIdentifier;
import io.github.ulisse1996.annotation.Dao;

@Dao
@MyIdentifier(CDIEntity.class)
public interface CdiQualifiedDAO extends BaseDao<CDIEntity> {
}
