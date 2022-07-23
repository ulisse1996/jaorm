package io.github.ulisse1996.jaorm.integration.test.cdi;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;

import javax.enterprise.context.ApplicationScoped;

@Dao
@ApplicationScoped
public interface CdiApplicationScopedDAO extends BaseDao<CDIEntity> {

    @Query(sql = "DELETE FROM WELD WHERE COL1 = ?")
    void deleteAll(String col1);
}
