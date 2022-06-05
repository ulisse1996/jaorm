package io.github.ulisse1996.jaorm.dsl.query.common.intermediate;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface CaseThen<R> {

    CaseElse<R> then(R value);
    CaseElse<R> then(SqlColumn<?, R> column);
    CaseElse<R> then(SqlColumn<?, R> column, String alias);
}
