package io.test;

import io.github.ulisse1996.annotation.Query;

public interface QueryWithSelectEntity {

    @Query(sql = "SELECT I FROM O WHERE E = ? AND ER = ?")
    io.test.SimpleEntity getFirst(String e, String er);
}
