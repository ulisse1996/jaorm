package io.github.ulisse1996.jaorm.processor.strategy;

import com.squareup.javapoet.CodeBlock;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GenerationUnit {

    private static final GenerationUnit EMPTY = new GenerationUnit(Collections.emptyList(), Collections.emptySet());
    private final List<CodeBlock> codeParts;
    private final Set<String> collectionNames;

    public GenerationUnit(List<CodeBlock> codeParts, Set<String> collectionNames) {
        this.codeParts = codeParts;
        this.collectionNames = collectionNames;
    }

    public static GenerationUnit ofCode(List<CodeBlock> codeParts) {
        return new GenerationUnit(codeParts, Collections.emptySet());
    }

    public static GenerationUnit empty() {
        return EMPTY;
    }

    public List<CodeBlock> getCodeParts() {
        return codeParts;
    }

    public Set<String> getCollectionNames() {
        return collectionNames;
    }
}
