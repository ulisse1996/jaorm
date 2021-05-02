package io.github.ulisse1996.integration.test.query;

import io.github.ulisse1996.annotation.Dao;
import io.github.ulisse1996.annotation.Query;
import io.github.ulisse1996.integration.test.entity.CustomAccessor;

import java.util.Optional;

@Dao
public interface CustomAccessorDAO {

    @Query(sql = "SELECT * FROM CUSTOM_ACCESSOR WHERE CUSTOM = ?")
    Optional<CustomAccessor> select(CustomAccessor.MyEnumCustom enumCustom);
}
