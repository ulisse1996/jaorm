package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.integration.test.entity.CascadeEntity;

@Dao
public interface CascadeDAO extends BaseDao<CascadeEntity> {

    @Query(sql = "SELECT * FROM CASCADE_ENTITY WHERE CASCADE_ID = ?")
    CascadeEntity findById(int id);
}
