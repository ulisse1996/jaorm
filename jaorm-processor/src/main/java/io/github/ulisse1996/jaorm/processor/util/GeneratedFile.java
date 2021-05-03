package io.github.ulisse1996.jaorm.processor.util;

import com.squareup.javapoet.TypeSpec;

public class GeneratedFile {

    private final String packageName;
    private final TypeSpec spec;
    private final String entityName;

    public GeneratedFile(String packageName, TypeSpec spec, String entityName) {
        this.packageName = packageName;
        this.spec = spec;
        this.entityName = entityName;
    }

    public String getPackageName() {
        return packageName;
    }

    public TypeSpec getSpec() {
        return spec;
    }

    public String getEntityName() {
        return entityName;
    }
}
