package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultListeners;
import io.github.ulisse1996.jaorm.spi.provider.ListenerProvider;

import java.util.Collections;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ListenersService {

    private static final Singleton<ListenersService> INSTANCE = Singleton.instance();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static ListenersService getInstance() {
        LOCK.lock();
        try {
            if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(INSTANCE.get().getEventClasses())) {
                try {
                    Iterable<ListenerProvider> iterable = ServiceFinder.loadServices(ListenerProvider.class);
                    if (iterable.iterator().hasNext()) {
                        INSTANCE.set(new DefaultListeners(iterable));
                    } else {
                        INSTANCE.set(NoOp.INSTANCE);
                    }
                } catch (Exception | ServiceConfigurationError ex) {
                    INSTANCE.set(NoOp.INSTANCE);
                }
            }
        } finally {
            LOCK.unlock();
        }

        return INSTANCE.get();
    }

    public void fireEvent(Object entity, GlobalEventType eventType) {
        Class<?> klass = entity.getClass();
        klass = getRealClass(klass);
        Class<?> finalKlass = klass;
        if (getEventClasses().stream().anyMatch(el -> el.equals(finalKlass))) {
            GlobalEventListener.getInstance().handleEvent(entity, eventType);
        }
    }

    private Class<?> getRealClass(Class<?> klass) {
        if (EntityDelegate.class.isAssignableFrom(klass)) {
            return DelegatesService.getInstance().getEntityClass(klass);
        }

        return klass;
    }

    public abstract Set<Class<?>> getEventClasses();

    private static class NoOp extends ListenersService {

        private static final NoOp INSTANCE = new NoOp();

        @Override
        public Set<Class<?>> getEventClasses() {
            return Collections.emptySet();
        }
    }
}
