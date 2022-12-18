package io.github.ulisse1996.jaorm.dsl.query.simple.intermediate;

public interface IntermediateSimpleHaving<T> {

    // ESimpleHaving Expression operations
    SimpleHaving eq(T val);
    SimpleHaving ne(T val);
    SimpleHaving lt(T val);
    SimpleHaving gt(T val);
    SimpleHaving le(T val);
    SimpleHaving ge(T val);

    // Standard operations
    SimpleHaving equalsTo(T val);
    SimpleHaving notEqualsTo(T val);
    SimpleHaving lessThan(T val);
    SimpleHaving greaterThan(T val);
    SimpleHaving lessOrEqualsTo(T val);
    SimpleHaving greaterOrEqualsTo(T val);
}
