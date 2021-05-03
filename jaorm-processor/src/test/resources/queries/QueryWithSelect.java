package io.test;

import io.github.ulisse1996.jaorm.annotation.Query;

public interface QueryWithSelect {

    @Query(sql = "SELECT I FROM O WHERE E = ? AND ER = ?")
    String getFirst(String e, String er);
}
