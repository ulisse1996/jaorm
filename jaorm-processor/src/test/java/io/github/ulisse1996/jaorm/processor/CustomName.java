package io.github.ulisse1996.jaorm.processor;

import javax.lang.model.element.Name;

public class CustomName implements Name {

    private final String field;

    public CustomName(String field) {
        this.field = field;
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
        return field.contentEquals(cs);
    }

    @Override
    public int length() {
        return field.length();
    }

    @Override
    public char charAt(int index) {
        return field.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return field.subSequence(start, end);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return field;
    }
}
