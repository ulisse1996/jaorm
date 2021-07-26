package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
import io.github.ulisse1996.jaorm.exception.GlobalEventException;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

public abstract class GlobalEventListener {

    private static final Singleton<GlobalEventListener> INSTANCE = Singleton.instance();

    public static synchronized GlobalEventListener getInstance() {
        if (!INSTANCE.isPresent()) {
            try {
                INSTANCE.set(ServiceFinder.loadService(GlobalEventListener.class));
            } catch (Exception ex) {
                INSTANCE.set(NoOp.INSTANCE);
            }
        }

        return INSTANCE.get();
    }

    public abstract void handleEvent(Object entity, GlobalEventType globalEventType) throws GlobalEventException;

    private static final class NoOp extends GlobalEventListener {

        private static final NoOp INSTANCE = new NoOp();

        @Override
        public void handleEvent(Object entity, GlobalEventType globalEventType) throws GlobalEventException {
            throw new UnsupportedOperationException();
        }
    }
}
