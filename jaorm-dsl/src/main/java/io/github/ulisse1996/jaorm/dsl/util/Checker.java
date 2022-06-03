package io.github.ulisse1996.jaorm.dsl.util;

public class Checker {

    private Checker() {}

    public static void assertNotNull(Object o, String name) {
        if (o == null) {
            throw new IllegalArgumentException(String.format("%s can't be null !", name));
        }
    }
}
