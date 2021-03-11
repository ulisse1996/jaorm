package io.jaorm.integration.test.cdi;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;

import javax.enterprise.context.RequestScoped;

@Dao
@RequestScoped
public interface CdiRequestScopedDAO extends BaseDao<CDIEntity> {
}