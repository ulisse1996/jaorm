package io.github.ulisse1996.jaorm.external;

import javax.lang.model.type.TypeMirror;

public abstract class LombokTypeMock extends LombokMock implements TypeMirror {

    protected LombokTypeMock() {
        super(null);
    }
}
