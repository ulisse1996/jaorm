package io.github.ulisse1996.jaorm.extension.cdi;

import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
import io.github.ulisse1996.jaorm.exception.GlobalEventException;
import io.github.ulisse1996.jaorm.spi.GlobalEventListener;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GlobalListenerService extends GlobalEventListener {

    @Override
    public void handleEvent(Object entity, GlobalEventType globalEventType) throws GlobalEventException {

    }
}
