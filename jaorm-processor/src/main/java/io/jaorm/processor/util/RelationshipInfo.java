package io.jaorm.processor.util;

import javax.lang.model.element.TypeElement;
import java.util.List;

public class RelationshipInfo {

    private final TypeElement type;
    private final List<RelationshipAccessor> relationships;

    public RelationshipInfo(TypeElement type, List<RelationshipAccessor> relationships) {
        this.type = type;
        this.relationships = relationships;
    }

    public TypeElement getType() {
        return type;
    }

    public List<RelationshipAccessor> getRelationships() {
        return relationships;
    }
}
