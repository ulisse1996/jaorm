package io.github.ulisse1996.jaorm.extension.micronaut;

import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
import io.github.ulisse1996.jaorm.exception.GlobalEventException;
import io.github.ulisse1996.jaorm.spi.GlobalEventListener;
import jakarta.inject.Singleton;

@Singleton
public class GlobalListenerService extends GlobalEventListener {

    @Override
    public void handleEvent(Object entity, GlobalEventType globalEventType) throws GlobalEventException {

    }
}
