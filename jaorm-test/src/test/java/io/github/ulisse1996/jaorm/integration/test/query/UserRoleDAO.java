package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.integration.test.entity.UserRole;
import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;

@Dao
public interface UserRoleDAO extends BaseDao<UserRole> {}
