package io.github.ulisse1996.jaorm.external.support.mock;

import com.squareup.javapoet.TypeName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.lang.model.type.TypeKind;
import java.util.Collections;

class VoidTypeTest {

    private final VoidType testSubject = VoidType.TYPE;

    @Test
    void should_return_void_kind() {
        Assertions.assertEquals(TypeKind.VOID, testSubject.getKind());
    }

    @Test
    void should_return_void_type_name() {
        Assertions.assertEquals(TypeName.VOID, testSubject.accept(null, null));
    }

    @Test
    void should_return_empty_annotation_mirrors() {
        Assertions.assertEquals(Collections.emptyList(), testSubject.getAnnotationMirrors());
    }

    @Test
    void should_return_null_annotation() {
        Assertions.assertNull(testSubject.getAnnotation(null));
    }

    @Test
    void should_return_empty_annotation_array() {
        Assertions.assertEquals(0, testSubject.getAnnotationsByType(null).length);
    }
}
