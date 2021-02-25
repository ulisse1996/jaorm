package io.jaorm.integration.test.spring;

import io.jaorm.BaseDao;
import io.jaorm.processor.annotation.Dao;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Dao
@Scope("prototype")
@Repository
public interface SpringPrototypeDAO extends BaseDao<SpringEntity> {
}
