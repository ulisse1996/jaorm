package io.github.ulisse1996.integration.test.cdi;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Dao;

import javax.enterprise.context.RequestScoped;

@Dao
@RequestScoped
public interface CdiRequestScopedDAO extends BaseDao<CDIEntity> {
}
