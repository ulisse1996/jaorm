package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.integration.test.entity.CustomAccessor;

import java.util.Optional;

@Dao
public interface CustomAccessorDAO {

    @Query(sql = "SELECT * FROM CUSTOM_ACCESSOR WHERE CUSTOM = ?")
    Optional<CustomAccessor> select(CustomAccessor.MyEnumCustom enumCustom);
}
