package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

public class DelegationInfo {

    private final List<MethodSpec> methods;
    private final List<CodeBlock> relationshipsBlocks;

    public DelegationInfo(List<MethodSpec> methods, List<CodeBlock> relationshipsBlocks) {
        this.methods = methods;
        this.relationshipsBlocks = relationshipsBlocks;
    }

    public List<CodeBlock> getRelationshipsBlocks() {
        return relationshipsBlocks;
    }

    public List<MethodSpec> getMethods() {
        return methods;
    }
}
