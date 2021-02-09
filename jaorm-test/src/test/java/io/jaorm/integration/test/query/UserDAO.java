package io.jaorm.integration.test.query;

import io.jaorm.BaseDao;
import io.jaorm.integration.test.entity.User;
import io.jaorm.processor.annotation.Dao;

@Dao
public interface UserDAO extends BaseDao<User> {}
