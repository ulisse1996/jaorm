package io.jaorm.integration.test.cdi;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

@Dao
@SessionScoped
public interface CdiSessionScopedDAO extends BaseDao<CDIEntity>, Serializable {
}
