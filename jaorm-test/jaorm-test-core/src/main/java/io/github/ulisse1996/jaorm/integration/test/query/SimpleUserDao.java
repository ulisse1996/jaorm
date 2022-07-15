package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.integration.test.entity.User;
import io.github.ulisse1996.jaorm.specialization.SingleKeyDao;

@Dao
public interface SimpleUserDao extends SingleKeyDao<User, Integer> {
}
