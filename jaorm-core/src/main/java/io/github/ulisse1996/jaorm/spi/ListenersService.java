package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.Collections;
import java.util.Set;

public abstract class ListenersService {

    private static final Singleton<ListenersService> INSTANCE = Singleton.instance();

    public static synchronized ListenersService getInstance() {
        if (!INSTANCE.isPresent()) {
            try {
                INSTANCE.set(ServiceFinder.loadService(ListenersService.class));
            } catch (Exception ex) {
                INSTANCE.set(NoOp.INSTANCE);
            }
        }

        return INSTANCE.get();
    }

    public void fireEvent(Object entity, GlobalEventType eventType) {
        Class<?> klass = entity.getClass();
        klass = getRealClass(klass);
        if (getEventClasses().contains(klass)) {
            GlobalEventListener.getInstance().handleEvent(entity, eventType);
        }
    }

    private Class<?> getRealClass(Class<?> klass) {
        if (EntityDelegate.class.isAssignableFrom(klass)) {
            return DelegatesService.getInstance().getEntityClass(klass);
        }

        return klass;
    }

    protected abstract Set<Class<?>> getEventClasses();

    private static class NoOp extends ListenersService {

        private static final NoOp INSTANCE = new NoOp();

        @Override
        protected Set<Class<?>> getEventClasses() {
            return Collections.emptySet();
        }
    }
}
