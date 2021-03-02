package io.test;

import io.jaorm.annotation.Query;

public interface QueryWithUpdate {

    @Query(sql = "UPDATE I SET E = ? WHERE E = ? AND ER = ?")
    void update(String one, String e, String er);
}