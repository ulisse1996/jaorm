package io.test;

import io.github.ulisse1996.jaorm.annotation.Query;
import io.test.SimpleEntity;

import java.util.stream.Stream;

public interface QueryWithStream {

    @Query(sql = "SELECT * FROM E WHERE ONE = ?")
    Stream<SimpleEntity> selectAll(String one);
}
