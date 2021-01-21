package io.test;

import io.jaorm.BaseDao;
import io.jaorm.processor.annotation.Query;

public interface QueryWithBaseDao extends BaseDao<io.test.SimpleEntity> {

    @Query(sql = "DELETE E WHERE ONE = ?")
    void deleteAll(String one);
}