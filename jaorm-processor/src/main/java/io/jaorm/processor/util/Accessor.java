package io.jaorm.processor.util;

import javax.lang.model.element.ExecutableElement;
import java.util.Map;

public class Accessor {

    private final String name;
    private final Map.Entry<ExecutableElement, ExecutableElement> getterSetter;
    private final boolean key;

    public Accessor(String name, Map.Entry<ExecutableElement, ExecutableElement> accessor, boolean key) {
        this.name = name;
        this.getterSetter = accessor;
        this.key = key;
    }

    public boolean isKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Map.Entry<ExecutableElement, ExecutableElement> getGetterSetter() {
        return getterSetter;
    }
}
