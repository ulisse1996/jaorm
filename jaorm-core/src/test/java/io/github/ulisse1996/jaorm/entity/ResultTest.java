package io.github.ulisse1996.jaorm.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

class ResultTest {

    @Test
    void should_create_empty_result() {
        Assertions.assertFalse(Result.of(null).isPresent());
    }

    @Test
    void should_create_full_result() {
        Assertions.assertTrue(Result.of(1).isPresent());
    }

    @Test
    void should_throw_exception_for_get_without_value() {
        Assertions.assertThrows(NoSuchElementException.class, () -> Result.of(null).get()); //NOSONAR
    }

    @Test
    void should_apply_flat_map() {
        Result<Integer> val = Result.of(1);
        val = val.flatMap(i -> Result.of(i + 1));
        Assertions.assertEquals(2, val.get());
    }

    @Test
    void should_return_empty_for_empty_result_flat_map() {
        Result<Integer> val = Result.of(null);
        val = val.flatMap(i -> Result.of(i + 1));
        Assertions.assertEquals(Result.empty(), val);
    }

    @Test
    void should_apply_map() {
        Result<Integer> val = Result.of(1);
        val = val.map(i -> i + 1);
        Assertions.assertEquals(2, val.get());
    }

    @Test
    void should_return_empty_for_empty_result_map() {
        Result<Integer> val = Result.of(null);
        val = val.map(i -> i + 1);
        Assertions.assertEquals(Result.empty(), val);
    }

    @Test
    void should_convert_to_optional() {
        Assertions.assertSame(Optional.empty(), Result.empty().toOptional());
    }

    @Test
    void should_call_consumer_for_result_with_value() {
        Result<Integer> val = Result.of(2);
        StringBuilder out = new StringBuilder("1");
        val.ifPresent(i -> out.append(" + 2"));
        Assertions.assertEquals("1 + 2", out.toString());
    }

    @Test
    void should_not_call_consumer_for_result_without_value() {
        Result<Integer> val = Result.of(null);
        StringBuilder out = new StringBuilder("1");
        val.ifPresent(i -> out.append(" + 2"));
        Assertions.assertEquals("1", out.toString());
    }

    @Test
    void should_return_else() {
        Result<Integer> empty = Result.empty();
        int res = empty.orElse(2);
        Assertions.assertEquals(2, res);
    }

    @Test
    void should_return_current_value() {
        Result<Integer> empty = Result.of(1);
        int res = empty.orElse(2);
        Assertions.assertEquals(1, res);
    }
}
