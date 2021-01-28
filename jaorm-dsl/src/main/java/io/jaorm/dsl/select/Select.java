package io.jaorm.dsl.select;

import io.jaorm.dsl.common.EndSelect;

public interface Select {

    <T> EndSelect<T> select(Class<T> klass);
}
