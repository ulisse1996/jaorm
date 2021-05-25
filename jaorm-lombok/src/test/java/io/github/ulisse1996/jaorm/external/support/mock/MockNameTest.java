package io.github.ulisse1996.jaorm.external.support.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MockNameTest {

    private final MockName testSubject = new MockName("test");

    @Test
    void should_check_content_equals() {
        Assertions.assertAll(
                () -> Assertions.assertFalse(testSubject.contentEquals("mm")),
                () -> Assertions.assertTrue(testSubject.contentEquals("test"))
        );
    }

    @Test
    void should_return_same_length() {
        Assertions.assertEquals("test".length(), testSubject.length());
    }

    @Test
    void should_return_same_char() {
        Assertions.assertEquals('t', testSubject.charAt(0));
    }

    @Test
    void should_return_sub_sequence() {
        Assertions.assertEquals("es", testSubject.subSequence(1, testSubject.length() - 1));
    }

    @Test
    void should_return_same_name() {
        Assertions.assertEquals("test", testSubject.toString());
    }
}
