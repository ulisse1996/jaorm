package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.integration.test.entity.User;
import io.github.ulisse1996.jaorm.mapping.TableRow;

import java.util.Optional;
import java.util.stream.Stream;

@Dao
public interface TableRowUserDao extends BaseDao<User> {

    @Query(sql = "SELECT * FROM USER_ENTITY WHERE USER_ID = ?")
    TableRow readById(int userId);

    @Query(sql = "SELECT * FROM USER_ENTITY WHERE USER_ID = ?")
    Stream<TableRow> readStreamById(int userId);

    @Query(sql = "SELECT * FROM USER_ENTITY WHERE USER_ID = ?")
    Optional<TableRow> readByIdOpt(int userId);
}
