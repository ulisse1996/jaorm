package io.github.ulisse1996.jaorm.util;

public class ClassChecker {

    private ClassChecker() {}

    public static Class<?> findClass(String name, ClassLoader loader) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
