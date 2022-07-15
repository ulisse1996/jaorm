package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.integration.test.entity.Role;
import io.github.ulisse1996.jaorm.annotation.Dao;

@Dao
public interface RoleDAO extends BaseDao<Role> {}
