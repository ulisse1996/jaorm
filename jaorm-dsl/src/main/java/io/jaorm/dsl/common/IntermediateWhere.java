package io.jaorm.dsl.common;

public interface IntermediateWhere<T> extends EndSelect<T> {

    Where<T> and(String column);
    Where<T> or(String column);
}
