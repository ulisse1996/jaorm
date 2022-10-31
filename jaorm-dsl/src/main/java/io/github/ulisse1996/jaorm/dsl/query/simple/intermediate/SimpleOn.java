package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public interface SimpleOn {

   <L> IntermediateSimpleJoin<L> on(SqlColumn<?, L> column);
   <L> IntermediateSimpleJoin<L> on(SqlColumn<?, L> column, String alias);
}
