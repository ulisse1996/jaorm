package io.github.ulisse1996.jaorm.integration.test.spring.repository;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.integration.test.spring.entity.SpringEntity;
import io.github.ulisse1996.jaorm.specialization.SingleKeyDao;
import org.springframework.stereotype.Repository;

@Dao
@Repository
public interface JAORMSpringRepository extends SingleKeyDao<SpringEntity, String> {
}
