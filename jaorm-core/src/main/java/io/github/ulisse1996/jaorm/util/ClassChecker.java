package io.github.ulisse1996.jaorm.util;

public class ClassChecker {

    private static final boolean IS_QUARKUS;

    private ClassChecker() {}

    static {
        IS_QUARKUS = hasClass("io.quarkus.arc.Arc");
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean hasClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static boolean isAssignable(Class<?> c1, Class<?> c2) {
        boolean same = c1.equals(c2);
        if (same) {
            return true;
        }

        if (IS_QUARKUS) {
            return c1.getName().equalsIgnoreCase(c2.getName());
        }

        return false;
    }

}
