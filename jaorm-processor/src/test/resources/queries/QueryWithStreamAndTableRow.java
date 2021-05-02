package io.test;

import io.github.ulisse1996.annotation.Query;
import io.github.ulisse1996.mapping.TableRow;
import io.test.SimpleEntity;

import java.util.stream.Stream;

public interface QueryWithStreamAndTableRow {

    @Query(sql = "SELECT * FROM E WHERE ONE = ?")
    Stream<TableRow> selectAll(String one);
}
