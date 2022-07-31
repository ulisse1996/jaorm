package io.github.ulisse1996.jaorm.processor.util;

import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.mapping.Cursor;
import io.github.ulisse1996.jaorm.mapping.TableRow;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

public class ReturnTypeDefinition {

    private static final String LIST_TYPE = java.util.List.class.getName();
    private static final String RESULT_TYPE = Result.class.getName();
    private static final String OPTIONAL_TYPE = java.util.Optional.class.getName();
    private static final String STREAM_TYPE = java.util.stream.Stream.class.getName();
    private static final String TABLE_ROW_TYPE = TableRow.class.getName();
    private static final String CURSOR_TYPE = Cursor.class.getName();
    private static final String[] SUPPORTED = new String[] {
            LIST_TYPE, RESULT_TYPE, OPTIONAL_TYPE, STREAM_TYPE, TABLE_ROW_TYPE, CURSOR_TYPE
    };

    private boolean simple;
    private boolean collection;
    private boolean optional;
    private boolean stream;
    private boolean tableRow;
    private boolean streamTableRow;
    private boolean cursor;
    private TypeElement realClass;

    public ReturnTypeDefinition(ProcessingEnvironment processingEnvironment, TypeMirror typeMirror) {
        String typeName = typeMirror.toString();
        boolean found = false;
        for (String regex : SUPPORTED) {
            if (found) {
                break;
            }
            if (typeName.contains(regex)) {
                found = true;
                checkType(processingEnvironment, typeName, regex);
            }
        }
        boolean plain = !optional && !collection && !stream && !streamTableRow && !tableRow && !cursor;
        if (plain) {
            this.simple = true;
            this.realClass = (TypeElement) processingEnvironment.getTypeUtils().asElement(typeMirror);
            if (realClass == null) {
                // Check for primitive return type
                PrimitiveType primitive = processingEnvironment.getTypeUtils().getPrimitiveType(typeMirror.getKind());
                this.realClass = processingEnvironment.getTypeUtils().boxedClass(primitive);
            }
        }
    }

    private void checkType(ProcessingEnvironment processingEnvironment, String typeName, String regex) {
        if (regex.contains("Result") || regex.contains("Optional")) {
            this.optional = true;
            this.realClass = asElement(processingEnvironment, regex, typeName);
            if (realClass.asType().toString().contains(TABLE_ROW_TYPE)) {
                this.tableRow = true;
            }
        } else if (regex.contains("List")) {
            this.realClass = asElement(processingEnvironment, regex, typeName);
            this.collection = true;
            if (realClass.asType().toString().contains(TABLE_ROW_TYPE)) {
                this.tableRow = true;
            }
        } else if (regex.contains("Stream")) {
            this.realClass = asElement(processingEnvironment, regex, typeName);
            this.stream = true;
            if (realClass.asType().toString().contains(TABLE_ROW_TYPE)) {
                this.streamTableRow = true;
            }
        } else if (regex.contains("Cursor")) {
            this.realClass = asElement(processingEnvironment, regex, typeName);
            this.cursor = true;
        } else {
            this.tableRow = true;
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

    public boolean isSimple() {
        return simple;
    }

    public boolean isCursor() {
        return cursor;
    }
}
