package io.github.ulisse1996.jaorm.integration.test.spring.repository;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.integration.test.spring.entity.SpringEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;

@Dao
@RequestScope
@Repository
public interface SpringRequestDAO extends BaseDao<SpringEntity> {
}
