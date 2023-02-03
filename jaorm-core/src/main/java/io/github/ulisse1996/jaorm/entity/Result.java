package io.github.ulisse1996.jaorm.entity;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

    public boolean isEmpty() {
        return entity == null;
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

    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (entity != null) {
            action.accept(entity);
        } else {
            emptyAction.run();
        }
    }

    public Result<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(entity) ? this : empty();
        }
    }

    public Result<T> or(Supplier<? extends Result<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent()) {
            return this;
        } else {
            @SuppressWarnings("unchecked")
            Result<T> r = (Result<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    public Stream<T> stream() {
        if (!isPresent()) {
            return Stream.empty();
        } else {
            return Stream.of(entity);
        }
    }

    public T orElse(T val) {
        return isPresent() ? this.entity : val;
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return entity != null ? entity : supplier.get();
    }

    public T orElseThrow() {
        if (entity == null) {
            throw new NoSuchElementException("No value present");
        }
        return entity;
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (entity != null) {
            return entity;
        } else {
            throw exceptionSupplier.get();
        }
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(this.entity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Result)) {
            return false;
        }

        Result<?> other = (Result<?>) obj;
        return Objects.equals(entity, other.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entity);
    }

    @Override
    public String toString() {
        return entity != null
                ? String.format("Optional[%s]", entity)
                : "Optional.empty";
    }
}
