package io.github.ulisse1996.jaorm.integration.test.cdi;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.integration.test.cdi.inject.MyIdentifier;
import io.github.ulisse1996.jaorm.annotation.Dao;

@Dao
@MyIdentifier(CDIEntity.class)
public interface CdiQualifiedDAO extends BaseDao<CDIEntity> {
}
