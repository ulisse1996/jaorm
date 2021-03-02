package io.jaorm.integration.test.spring;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Dao
@Scope("singleton")
@Repository
public interface SpringSingletonDAO extends BaseDao<SpringEntity> {
}
