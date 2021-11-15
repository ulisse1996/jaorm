package io.github.ulisse1996.jaorm.external.support;

import io.github.ulisse1996.jaorm.external.LombokSupport;
import io.github.ulisse1996.jaorm.external.support.mock.MockGetter;
import io.github.ulisse1996.jaorm.external.support.mock.MockSetter;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class LombokGenerator extends LombokSupport {

    @Override
    public boolean isLombokGenerated(Element element) {
        if (!element.getKind().isField()) {
            return false;
        }

        return isGetterSetterAnnotated(element) || isDataClass(element);
    }

    @Override
    public boolean hasLombokNoArgs(TypeElement entity) {
        return entity.getAnnotation(NoArgsConstructor.class) != null;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    private boolean isDataClass(Element element) {
        Element entity = element.getEnclosingElement();
        return entity.getAnnotation(Data.class) != null;
    }

    private boolean isGetterSetterAnnotated(Element element) {
        return element.getAnnotation(Getter.class) != null
                || element.getAnnotation(Setter.class) != null;
    }

    @Override
    public Element generateFakeElement(Element element, GenerationType generationType) {
        if (GenerationType.GETTER.equals(generationType)) {
            return new MockGetter(element);
        } else {
            return new MockSetter(element);
        }
    }
}
