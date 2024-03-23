package io.github.ulisse1996.jaorm;

public class InlineValueWithAlias<R> implements InlineValue<R> {

    private final R value;
    private final String alias;

    InlineValueWithAlias(R value, String alias) {
        this.value = value;
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public R getValue() {
        return this.value;
    }
}
