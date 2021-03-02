package io.test;

import io.jaorm.annotation.Query;

public interface QueryWithUnknownStrategy {

    @Query(sql = "SELECT ONE WHERE ONE = ONE")
    String delete(String e);
}