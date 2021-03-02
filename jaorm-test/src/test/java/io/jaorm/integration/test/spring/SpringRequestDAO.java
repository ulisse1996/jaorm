package io.jaorm.integration.test.spring;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;

@Dao
@RequestScope
@Repository
public interface SpringRequestDAO extends BaseDao<SpringEntity> {
}
