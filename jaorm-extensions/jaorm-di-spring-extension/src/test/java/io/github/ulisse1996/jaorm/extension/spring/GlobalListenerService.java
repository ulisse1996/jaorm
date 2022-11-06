package io.github.ulisse1996.jaorm.extension.spring;

import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
import io.github.ulisse1996.jaorm.exception.GlobalEventException;
import io.github.ulisse1996.jaorm.spi.GlobalEventListener;
import org.springframework.stereotype.Service;

@Service
public class GlobalListenerService extends GlobalEventListener {

    @Override
    public void handleEvent(Object entity, GlobalEventType globalEventType) throws GlobalEventException {

    }
}
