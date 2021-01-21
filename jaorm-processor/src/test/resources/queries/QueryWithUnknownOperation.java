package io.test;

import io.jaorm.processor.annotation.Query;

public interface QueryWithUnknownOperation {

    @Query(sql = "SOME I SET ER = ? WHERE E = ? AND ER = ?")
    String update(String one, String e, String er);
}