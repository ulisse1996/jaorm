package io.test;

import io.jaorm.annotation.Query;

public interface QueryWithSelectPrimitive {

    @Query(sql = "SELECT I FROM O WHERE E = ? AND ER = ?")
    int getFirst(String e, String er);
}
