package io.test;

import io.github.ulisse1996.jaorm.annotation.Query;

import java.util.Optional;

public interface QueryWithSelectOptional {

    @Query(sql = "SELECT I FROM O WHERE E = ? AND ER = ?")
    Optional<String> getFirst(String e, String er);
}
