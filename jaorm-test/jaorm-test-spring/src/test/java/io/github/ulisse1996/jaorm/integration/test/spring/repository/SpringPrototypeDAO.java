package io.github.ulisse1996.jaorm.integration.test.spring.repository;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.integration.test.spring.entity.SpringEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Dao
@Scope("prototype")
@Repository
public interface SpringPrototypeDAO extends BaseDao<SpringEntity> {
}
