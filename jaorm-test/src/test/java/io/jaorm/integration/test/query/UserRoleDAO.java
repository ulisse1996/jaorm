package io.jaorm.integration.test.query;

import io.jaorm.BaseDao;
import io.jaorm.integration.test.entity.UserRole;
import io.jaorm.annotation.Dao;

@Dao
public interface UserRoleDAO extends BaseDao<UserRole> {}
