package io.test;

import io.jaorm.annotation.Query;

import java.util.List;

public interface QueryWithSelectList {

    @Query(sql = "SELECT I FROM O WHERE E = ? AND ER = ?")
    List<String> getFirst(String e, String er);
}