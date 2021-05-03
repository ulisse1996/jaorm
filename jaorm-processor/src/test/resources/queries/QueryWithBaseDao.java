package io.test;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Query;

public interface QueryWithBaseDao extends BaseDao<io.test.SimpleEntity> {

    @Query(sql = "DELETE E WHERE ONE = ?")
    void deleteAll(String one);
}
