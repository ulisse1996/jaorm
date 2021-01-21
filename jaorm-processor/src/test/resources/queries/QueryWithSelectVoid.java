package io.test;

import io.jaorm.processor.annotation.Query;

public interface QueryWithSelectVoid {

    @Query(sql = "SELECT I FROM O WHERE E = ? AND ER = ?")
    void getFirst(String e, String er);
}