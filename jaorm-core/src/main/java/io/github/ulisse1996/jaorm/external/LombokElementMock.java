package io.github.ulisse1996.jaorm.external;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import java.util.Collections;
import java.util.List;

public abstract class LombokElementMock extends LombokMock implements Element {

    protected LombokElementMock(Element element) {
        super(element);
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return Collections.emptyList();
    }


    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return null;
    }
}
