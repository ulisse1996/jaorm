package io.test;

import io.jaorm.processor.annotation.Query;

public interface QueryWithUnknownStrategy {

    @Query(sql = "SELECT ONE WHERE ONE = ONE")
    String delete(String e);
}