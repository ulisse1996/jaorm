package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.List;

@SuppressWarnings("unused")
public interface CaseEnd<R> {

    Pair<String, List<SqlParameter>> doBuild(String table, boolean caseInsensitive);
}
