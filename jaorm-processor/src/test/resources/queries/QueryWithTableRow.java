package io.test;

import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.mapping.TableRow;
import io.test.SimpleEntity;

public interface QueryWithTableRow {

    @Query(sql = "SELECT * FROM E WHERE ONE = ?")
    TableRow select(String one);
}
