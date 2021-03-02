package io.jaorm.integration.test.spring;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.ApplicationScope;

@Dao
@ApplicationScope
@Repository
public interface SpringApplicationDAO extends BaseDao<SpringEntity> {
}
