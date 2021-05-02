package io.github.ulisse1996.integration.test.query;

import io.github.ulisse1996.integration.test.entity.User;
import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Dao;

@Dao
public interface UserDAO extends BaseDao<User> {}
