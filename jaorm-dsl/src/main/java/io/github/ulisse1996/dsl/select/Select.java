package io.github.ulisse1996.dsl.select;

import io.github.ulisse1996.dsl.common.EndSelect;

public interface Select {

    <T> EndSelect<T> select(Class<T> klass);
}
