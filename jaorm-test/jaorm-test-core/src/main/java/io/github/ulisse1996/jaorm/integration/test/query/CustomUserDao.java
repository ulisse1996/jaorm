package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.integration.test.entity.User;

import java.util.Optional;

@Dao
public interface CustomUserDao {

    @Query(sql = "mySql.sql")
    Optional<User> getUserOpt(int userId);
}
