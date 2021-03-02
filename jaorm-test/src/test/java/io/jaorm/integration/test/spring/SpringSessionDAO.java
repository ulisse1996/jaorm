package io.jaorm.integration.test.spring;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.SessionScope;

@Dao
@SessionScope
@Repository
public interface SpringSessionDAO extends BaseDao<SpringEntity> {
}
