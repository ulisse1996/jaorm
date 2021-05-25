package io.github.ulisse1996.jaorm.external.support.mock;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;

public class MockSetter extends MockAccessor {

    public MockSetter(Element element) {
        super(element, false);
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        return Collections.singletonList(new MockParameter(this, element));
    }

    @Override
    public TypeMirror getReturnType() {
        return VoidType.TYPE;
    }
}
