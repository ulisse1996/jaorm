package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.integration.test.entity.CascadeEntity;
import io.github.ulisse1996.jaorm.integration.test.entity.CascadeEntityInner;

import java.util.Optional;

@Dao
public interface CascadeInnerDAO extends BaseDao<CascadeEntityInner> {

    @Query(sql = "SELECT * FROM CASCADE_ENTITY_INNER WHERE CASCADE_ID = ?")
    Optional<CascadeEntity> findById(int id);
}
