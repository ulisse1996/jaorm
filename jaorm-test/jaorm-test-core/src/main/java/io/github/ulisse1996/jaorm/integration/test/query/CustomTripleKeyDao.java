package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.integration.test.entity.TripleIdEntity;
import io.github.ulisse1996.jaorm.specialization.TripleKeyDao;

@Dao
public interface CustomTripleKeyDao extends TripleKeyDao<TripleIdEntity, Long, Long, String> {
}
