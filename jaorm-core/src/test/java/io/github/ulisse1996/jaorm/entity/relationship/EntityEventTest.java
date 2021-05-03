package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.spi.DelegatesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class EntityEventTest {

    @Test
    void should_return_compatible_type() {
        Assertions.assertAll(
                () -> Assertions.assertTrue(EntityEvent.forType(EntityEventType.PERSIST) instanceof PersistEvent),
                () -> Assertions.assertTrue(EntityEvent.forType(EntityEventType.REMOVE) instanceof RemoveEvent),
                () -> Assertions.assertTrue(EntityEvent.forType(EntityEventType.UPDATE) instanceof UpdateEvent)
        );
    }

    @Test
    void should_return_real_class() {
        DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.getEntityClass(Mockito.any()))
                    .then(invocation -> Object.class);

            Assertions.assertEquals(Object.class, EntityEvent.forType(EntityEventType.PERSIST).getRealClass(Object.class));
        }
    }
}
