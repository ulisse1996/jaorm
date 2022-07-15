package io.github.ulisse1996.jaorm.integration.test.spring;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;

@Dao
@RequestScope
@Repository
public interface SpringRequestDAO extends BaseDao<SpringEntity> {
}
