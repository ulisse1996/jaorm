package io.github.ulisse1996.integration.test.cdi;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.annotation.Dao;
import io.github.ulisse1996.annotation.Query;

import javax.enterprise.context.ApplicationScoped;

@Dao
@ApplicationScoped
public interface CdiApplicationScopedDAO extends BaseDao<CDIEntity> {

    @Query(sql = "DELETE FROM WELD WHERE COL1 = ?")
    void deleteAll(String col1);
}
