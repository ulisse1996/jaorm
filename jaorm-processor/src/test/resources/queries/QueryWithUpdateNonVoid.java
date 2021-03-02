package io.test;

import io.jaorm.annotation.Query;

public interface QueryWithUpdateNonVoid {

    @Query(sql = "UPDATE I SET ER = ? WHERE E = ? AND ER = ?")
    String update(String one, String e, String er);
}