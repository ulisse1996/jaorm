package io.github.ulisse1996.jaorm.integration.test.micronaut.entity;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import jakarta.inject.Singleton;

@Singleton
@Dao
public interface MicronautRepository extends BaseDao<MicronautEntity> {
}
