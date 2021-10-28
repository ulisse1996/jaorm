package io.github.ulisse1996.jaorm.integration.test.projection;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;

import java.util.List;
import java.util.Optional;

@Dao
public interface ProjectionDao {

    @Query(sql = "SELECT ID_COL, SUB_NAME, VALID FROM LONG_TABLE", noArgs = true)
    MyProjection getMyProjection();

    @Query(sql = "SELECT ID_COL, SUB_NAME, VALID FROM LONG_TABLE", noArgs = true)
    Optional<MyProjection> getOptMyProjection();

    @Query(sql = "SELECT ID_COL, SUB_NAME, VALID FROM LONG_TABLE", noArgs = true)
    List<MyProjection> getAllMyProjection();
}
