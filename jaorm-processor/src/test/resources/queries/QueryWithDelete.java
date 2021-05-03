package io.test;

import io.github.ulisse1996.jaorm.annotation.Query;

public interface QueryWithDelete {

    @Query(sql = "DELETE I WHERE E = ? AND ER = ?")
    void delete(String e, String er);
}
