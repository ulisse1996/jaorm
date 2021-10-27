package io.github.ulisse1996.jaorm.spi.combined;

import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.spi.RelationshipService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class CombinedRelationshipsTest {

    @Mock private RelationshipService mock1;
    @Mock private RelationshipService mock2;

    @Test
    void should_return_matched_relationship() {
        Relationship<?> mock = Mockito.mock(Relationship.class);
        Mockito.when(mock2.getRelationships(Mockito.any()))
                .then(invocation -> mock);
        CombinedRelationships combinedRelationships = new CombinedRelationships(Arrays.asList(mock1, mock2));
        Assertions.assertEquals(mock, combinedRelationships.getRelationships(Object.class));
    }

    @Test
    void should_not_find_matched_relationship() {
        CombinedRelationships combinedRelationships = new CombinedRelationships(Arrays.asList(mock1, mock2));
        Assertions.assertNull(combinedRelationships.getRelationships(Object.class));
    }
}
