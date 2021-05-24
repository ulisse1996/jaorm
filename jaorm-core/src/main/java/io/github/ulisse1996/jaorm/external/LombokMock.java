package io.github.ulisse1996.jaorm.external;

import javax.lang.model.element.Element;

public abstract class LombokMock {

    protected final Element element;

    protected LombokMock(Element element) {
        this.element = element;
    }
}
