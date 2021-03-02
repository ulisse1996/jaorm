package io.test;

import io.jaorm.annotation.Query;

public interface QueryWithSelectVoid {

    @Query(sql = "SELECT I FROM O WHERE E = ? AND ER = ?")
    void getFirst(String e, String er);
}