package io.github.ulisse1996.integration.test.cdi;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Dao;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

@Dao
@SessionScoped
public interface CdiSessionScopedDAO extends BaseDao<CDIEntity>, Serializable {
}
