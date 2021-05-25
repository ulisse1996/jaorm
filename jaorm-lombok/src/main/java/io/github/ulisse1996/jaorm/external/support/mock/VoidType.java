package io.github.ulisse1996.jaorm.external.support.mock;

import com.squareup.javapoet.TypeName;
import io.github.ulisse1996.jaorm.external.LombokTypeMock;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

public class VoidType extends LombokTypeMock implements NoType {

    public static final VoidType TYPE = new VoidType();

    private VoidType() {}

    @Override
    public TypeKind getKind() {
        return TypeKind.VOID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return (R) TypeName.VOID;
    }
}
