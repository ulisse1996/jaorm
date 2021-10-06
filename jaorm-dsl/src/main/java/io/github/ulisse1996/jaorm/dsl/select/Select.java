package io.github.ulisse1996.jaorm.dsl.select;

import io.github.ulisse1996.jaorm.dsl.common.EndSelect;

public interface Select {

    <T> EndSelect<T> select(Class<T> klass, boolean caseInsensitiveLike);
}
