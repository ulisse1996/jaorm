package io.github.ulisse1996.jaorm.dsl.query.common.trait;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;

public interface WithConfiguration<S> {

    S withConfiguration(QueryConfig config);
}
