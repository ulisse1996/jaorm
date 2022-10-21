package io.github.ulisse1996.jaorm;

public interface InlineValue<R> extends Selectable<R> {

    R getValue();

    static <R> InlineValue<R> inline(R value) {
        return () -> value;
    }
}
