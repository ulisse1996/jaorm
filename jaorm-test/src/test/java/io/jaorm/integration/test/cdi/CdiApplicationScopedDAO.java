package io.jaorm.integration.test.cdi;

import io.jaorm.BaseDao;
import io.jaorm.processor.annotation.Dao;

import javax.enterprise.context.ApplicationScoped;

@Dao
@ApplicationScoped
public interface CdiApplicationScopedDAO extends BaseDao<CDIEntity> {
}
