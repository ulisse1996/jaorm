package io.test;

import io.jaorm.BaseDao;
import io.jaorm.annotation.Query;
import io.jaorm.mapping.TableRow;
import io.test.SimpleEntity;

import java.util.stream.Stream;

public interface QueryWithTableRow {

    @Query(sql = "SELECT * FROM E WHERE ONE = ?")
    TableRow select(String one);
}