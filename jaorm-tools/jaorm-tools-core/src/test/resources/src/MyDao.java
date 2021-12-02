package io.test;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;

@Dao
public class MyDao extends BaseDao<Object> {

    @Query(sql = "SELECT COL1, COL2 FROM TAB1 JOIN TAB1 ON COL3 = COL1", noArgs = true)
    void queryNoAliasJoin();

    @Query(sql = "SELECT COL1, COL2 FROM TAB1", noArgs = true)
    void querySimple();

    @Query(sql = "SELECT AL.COL1, AL.COL2 FROM TAB1 AL JOIN TAB2 AM ON AM.CODE1 = AL.COL1", noArgs = true)
    void queryWithAlias();

    @Query(sql = "SELECT COL1 FROM TAB1 WHERE COL2 IN (SELECT COL3 FROM TAB2)", noArgs = true)
    void queryWithSubQuery();
}
