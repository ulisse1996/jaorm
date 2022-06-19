package io.github.ulisse1996.jaorm.spi.impl;

import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.spi.RelationshipService;
import io.github.ulisse1996.jaorm.spi.provider.RelationshipProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class DefaultRelationshipsTest {

    @Mock private Relationship<?> relationship;
    @Mock private RelationshipProvider provider;
    private RelationshipService service;

    @BeforeEach
    void init() {
        Mockito.when(provider.getEntityClass()).then(invocation -> BigDecimal.class);
        Mockito.when(provider.getRelationship()).then(invocation -> relationship);
        this.service = new DefaultRelationships(Collections.singleton(provider));
    }

    @Test
    void should_return_relationship() {
        Assertions.assertEquals(relationship,
                service.getRelationships(BigDecimal.class));
    }
}
