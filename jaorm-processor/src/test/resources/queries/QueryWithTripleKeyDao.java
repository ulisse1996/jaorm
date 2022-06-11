package io.test;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.specialization.TripleKeyDao;

@Dao
public interface QueryWithTripleKeyDao extends TripleKeyDao<io.test.TripleSimpleEntity, String, String, String> {}
