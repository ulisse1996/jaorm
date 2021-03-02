package io.jaorm.integration.test.cdi;

import io.jaorm.BaseDao;
import io.jaorm.integration.test.inject.MyIdentifier;
import io.jaorm.annotation.Dao;

@Dao
@MyIdentifier(CDIEntity.class)
public interface CdiQualifiedDAO extends BaseDao<CDIEntity> {
}
