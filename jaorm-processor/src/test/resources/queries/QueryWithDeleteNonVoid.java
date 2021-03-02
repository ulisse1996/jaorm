package io.test;

import io.jaorm.annotation.Query;

public interface QueryWithDeleteNonVoid {

    @Query(sql = "DELETE I WHERE E = ? AND ER = ?")
    String delete(String e, String er);
}