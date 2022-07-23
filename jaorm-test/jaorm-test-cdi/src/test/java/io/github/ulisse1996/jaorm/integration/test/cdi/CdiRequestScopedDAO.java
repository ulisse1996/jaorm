package io.github.ulisse1996.jaorm.integration.test.cdi;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;

import javax.enterprise.context.RequestScoped;

@Dao
@RequestScoped
public interface CdiRequestScopedDAO extends BaseDao<CDIEntity> {
}
