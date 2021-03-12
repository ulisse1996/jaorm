package io.jaorm.processor.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class ReturnTypeDefinition {

    private static final String[] SUPPORTED = new String[] {
            "java.util.List",
            "java.util.Optional",
            "java.util.stream.Stream",
            "io.jaorm.mapping.TableRow"
    };

    private boolean collection;
    private boolean optional;
    private boolean stream;
    private boolean tableRow;
    private boolean streamTableRow;
    private TypeElement realClass;

    public ReturnTypeDefinition(ProcessingEnvironment processingEnvironment, TypeMirror typeMirror) {
        String typeName = typeMirror.toString();
        for (String regex : SUPPORTED) {
            if (typeName.contains(regex)) {
                if (regex.contains("Optional")) {
                    this.optional = true;
                    this.realClass = asElement(processingEnvironment, regex, typeName);
                } else if (regex.contains("List")) {
                    this.realClass = asElement(processingEnvironment, regex, typeName);
                    this.collection = true;
                } else if (regex.contains("Stream")) {
                    this.realClass = asElement(processingEnvironment, regex, typeName);
                    this.stream = true;
                    if (realClass.asType().toString().contains(SUPPORTED[3])) {
                        this.streamTableRow = true;
                    }
                } else {
                    this.tableRow = true;
                }
            }
        }
        boolean plain = !optional && !collection && !stream && !streamTableRow;
        if (plain) {
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

    public boolean isStream() {
        return stream;
    }

    public boolean isTableRow() {
        return tableRow;
    }

    public boolean isStreamTableRow() {
        return streamTableRow;
    }
}
