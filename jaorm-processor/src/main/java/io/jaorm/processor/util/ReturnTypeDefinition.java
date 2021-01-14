package io.jaorm.processor.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class ReturnTypeDefinition {

    private static final String[] SUPPORTED = new String[] {
            "java.util.List",
            "java.util.Optional"
    };

    private boolean collection;
    private boolean optional;
    private final boolean plain;
    private TypeElement realClass;

    public ReturnTypeDefinition(ProcessingEnvironment processingEnvironment, TypeMirror typeMirror) {
        String typeName = typeMirror.toString();
        for (String regex : SUPPORTED) {
            if (typeName.contains(regex)) {
                if (regex.contains("Optional")) {
                    this.optional = true;
                } else {
                    this.collection = true;
                }
                this.realClass = asElement(processingEnvironment, regex, typeName);
            }
        }
        this.plain = !optional && !collection;
        if (this.plain) {
            this.realClass = (TypeElement) processingEnvironment.getTypeUtils().asElement(typeMirror);
        }
    }

    private TypeElement asElement(ProcessingEnvironment processingEnvironment, String regex, String typeName) {
        return processingEnvironment.getElementUtils().getTypeElement(
                typeName.replace(regex, "").replace("<", "").replace(">", ""));
    }

    public TypeElement getRealClass() {
        return realClass;
    }

    public boolean isCollection() {
        return collection;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isPlain() {
        return plain;
    }
}
