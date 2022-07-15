package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.MergeEndMatching;
import io.github.ulisse1996.jaorm.dsl.query.common.MergeEndNotMatching;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface MergedOn<T> {

    MergedOn<T> onEquals(SqlColumn<T, ?> column);

    MergeEndNotMatching<T> notMatchInsert(T entity);
    MergeEndMatching<T> matchUpdate(T entity);
}
