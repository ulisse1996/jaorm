package io.github.ulisse1996.jaorm.dsl.util;

public class Checker {

    private Checker() {}

    public static <T> T assertNotNull(T o, String name) {
        if (o == null) {
            throw new IllegalArgumentException(String.format("%s can't be null !", name));
        }
        return o;
    }
}
