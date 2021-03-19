package io.jaorm.integration.test.cdi;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Dao;
import io.jaorm.annotation.Query;

import javax.enterprise.context.ApplicationScoped;

@Dao
@ApplicationScoped
public interface CdiApplicationScopedDAO extends BaseDao<CDIEntity> {

    @Query(sql = "DELETE FROM WELD WHERE COL1 = ?")
    void deleteAll(String col1);
}
