package io.github.ulisse1996.jaorm.external.support.mock;

import javax.lang.model.element.Element;

public class MockGetter extends MockAccessor {

    public MockGetter(Element element) {
        super(element, true);
    }
}
