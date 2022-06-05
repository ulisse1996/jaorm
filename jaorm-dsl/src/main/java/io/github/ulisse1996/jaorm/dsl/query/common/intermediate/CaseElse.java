package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.dsl.query.common.Case;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface CaseElse<R> extends Case<R> {

    CaseEnd<R> orElse(R value);
    CaseEnd<R> orElse(SqlColumn<?, R> column);
    CaseEnd<R> orElse(SqlColumn<?, R> column, String alias);
}
