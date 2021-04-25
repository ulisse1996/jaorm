package io.test;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Query;
import io.test.SimpleEntity;

public interface QueryWithNoArgs extends BaseDao<io.test.SimpleEntity> {

    @Query(sql = "DELETE E", noArgs = true)
    void deleteAll();
}
