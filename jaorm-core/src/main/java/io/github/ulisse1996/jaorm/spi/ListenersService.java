package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultListeners;
import io.github.ulisse1996.jaorm.spi.provider.ListenerProvider;
import io.github.ulisse1996.jaorm.util.ClassChecker;

import java.util.Collections;
import java.util.ServiceConfigurationError;
import java.util.Set;

public abstract class ListenersService {

    private static final Singleton<ListenersService> INSTANCE = Singleton.instance();

    public static synchronized ListenersService getInstance() {
        if (!INSTANCE.isPresent()) {
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

        return INSTANCE.get();
    }

    public void fireEvent(Object entity, GlobalEventType eventType) {
        Class<?> klass = entity.getClass();
        klass = getRealClass(klass);
        Class<?> finalKlass = klass;
        if (getEventClasses().stream().anyMatch(el -> ClassChecker.isAssignable(el, finalKlass))) {
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
