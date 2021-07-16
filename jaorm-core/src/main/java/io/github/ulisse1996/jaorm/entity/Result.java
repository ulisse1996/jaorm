package io.github.ulisse1996.jaorm.entity;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Result<T> {

    private static final Result<?> EMPTY = new Result<>(null);
    private final T entity;

    public static <T> Result<T> of(T entity) {
        return entity == null ? empty() : new Result<>(entity);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> empty() {
        return (Result<T>) EMPTY;
    }

    private Result(T entity) {
        this.entity = entity;
    }

    public boolean isPresent() {
        return entity != null;
    }

    public T get() {
        if (this.entity == null) {
            throw new NoSuchElementException("No value present");
        }
        return this.entity;
    }

    public<U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Objects.requireNonNull(mapper.apply(this.entity));
        }
    }

    public<U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Result.of(mapper.apply(this.entity));
        }
    }

    public void ifPresent(Consumer<T> consumer) {
        if (isPresent()) {
            consumer.accept(entity);
        }
    }

    public T orElse(T val) {
        return isPresent() ? this.entity : val;
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(this.entity);
    }
}
