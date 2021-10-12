package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.List;

public interface WithSubQuerySupport {

    String getSql();
    List<SqlParameter> getParameters();
}
