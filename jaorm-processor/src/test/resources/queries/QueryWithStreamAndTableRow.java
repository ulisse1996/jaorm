package io.test;

import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.mapping.TableRow;
import io.test.SimpleEntity;

import java.util.stream.Stream;

public interface QueryWithStreamAndTableRow {

    @Query(sql = "SELECT * FROM E WHERE ONE = ?")
    Stream<TableRow> selectAll(String one);
}
