package io.github.ulisse1996.integration.test.spring;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Dao;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.SessionScope;

@Dao
@SessionScope
@Repository
public interface SpringSessionDAO extends BaseDao<SpringEntity> {
}
