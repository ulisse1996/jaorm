package io.jaorm.integration.test.query;

import io.jaorm.BaseDao;
import io.jaorm.integration.test.entity.Role;
import io.jaorm.annotation.Dao;

@Dao
public interface RoleDAO extends BaseDao<Role> {}
