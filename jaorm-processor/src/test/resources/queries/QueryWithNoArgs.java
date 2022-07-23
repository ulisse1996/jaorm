package io.test;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.test.SimpleEntity;

public interface QueryWithNoArgs extends BaseDao<io.test.SimpleEntity> {

    @Query(sql = "DELETE FROM MY_TABLE", noArgs = true)
    void deleteAll();
}
