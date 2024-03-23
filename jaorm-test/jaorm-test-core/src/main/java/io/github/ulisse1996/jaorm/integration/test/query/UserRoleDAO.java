package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.integration.test.entity.UserRole;
import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.specialization.DoubleKeyDao;
import io.github.ulisse1996.jaorm.specialization.SingleKeyDao;
import org.springframework.data.relational.core.sql.In;

@Dao
public interface UserRoleDAO extends DoubleKeyDao<UserRole, Integer, Integer> {}
