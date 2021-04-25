package io.jaorm.integration.test.query;

import io.jaorm.annotation.Dao;
import io.jaorm.annotation.Query;
import io.jaorm.integration.test.entity.CustomAccessor;

import java.util.Optional;

@Dao
public interface CustomAccessorDAO {

    @Query(sql = "SELECT * FROM CUSTOM_ACCESSOR WHERE CUSTOM = ?")
    Optional<CustomAccessor> select(CustomAccessor.MyEnumCustom enumCustom);
}
