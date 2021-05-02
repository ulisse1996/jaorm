package io.github.ulisse1996.integration.test.spring;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Dao;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Dao
@Scope("singleton")
@Repository
public interface SpringSingletonDAO extends BaseDao<SpringEntity> {
}
