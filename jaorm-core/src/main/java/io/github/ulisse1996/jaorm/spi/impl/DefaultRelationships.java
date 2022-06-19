package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.RelationshipService;
import io.github.ulisse1996.jaorm.spi.provider.RelationshipProvider;
import io.github.ulisse1996.jaorm.util.ClassChecker;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultRelationships extends RelationshipService {

    private static final JaormLogger logger = JaormLogger.getLogger(DefaultRelationships.class);
    private final Map<Class<?>, Relationship<?>> relationships;

    public DefaultRelationships(Iterable<RelationshipProvider> providers) {
        this.relationships = Collections.unmodifiableMap(
                StreamSupport.stream(providers.spliterator(), false)
                        .collect(Collectors.toMap(
                                RelationshipProvider::getEntityClass,
                                RelationshipProvider::getRelationship
                        ))
        );

        logger.debug(() -> String.format("Loaded relationships for %s", relationships.keySet()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Relationship<T> getRelationships(Class<T> entityClass) {
        return (Relationship<T>) relationships
                .entrySet()
                .stream()
                .filter(el -> ClassChecker.isAssignable(el.getKey(), entityClass))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }
}
