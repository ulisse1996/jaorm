package io.test;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Query;
import io.test.SimpleEntity;

public interface QueryWithNoArgs extends BaseDao<io.test.SimpleEntity> {

    @Query(sql = "DELETE E", noArgs = true)
    void deleteAll();
}
