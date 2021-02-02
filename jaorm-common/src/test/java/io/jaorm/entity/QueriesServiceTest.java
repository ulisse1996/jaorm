package io.jaorm.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QueriesServiceTest {

    @Test
    void should_return_query() {
        String expected = "1";
        String result = QueriesService.getInstance().getQuery(String.class);
        Assertions.assertSame(expected, result);
    }

    @Test
    void should_not_found_query() {
        try {
            QueriesService.getInstance().getQuery(Object.class);
        } catch (Exception ex) {
            Assertions.assertTrue(ex instanceof IllegalArgumentException);
            return;
        }

        Assertions.fail();
    }
}