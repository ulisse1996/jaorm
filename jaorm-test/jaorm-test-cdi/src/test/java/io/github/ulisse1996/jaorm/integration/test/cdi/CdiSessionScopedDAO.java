package io.github.ulisse1996.jaorm.integration.test.cdi;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

@Dao
@SessionScoped
public interface CdiSessionScopedDAO extends BaseDao<CDIEntity>, Serializable {
}
