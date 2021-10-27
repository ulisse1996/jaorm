package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.event.GlobalEventType;
import io.github.ulisse1996.jaorm.spi.combined.CombinedListeners;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class ListenersService {

    private static final Singleton<ListenersService> INSTANCE = Singleton.instance();

    public static synchronized ListenersService getInstance() {
        if (!INSTANCE.isPresent()) {
            try {
                Iterable<ListenersService> iterable = ServiceFinder.loadServices(ListenersService.class);
                if (iterable.iterator().hasNext()) {
                    List<ListenersService> services = StreamSupport.stream(iterable.spliterator(), false)
                            .collect(Collectors.toList());
                    if (services.size() == 1) {
                        INSTANCE.set(services.get(0));
                    } else {
                        INSTANCE.set(new CombinedListeners(services));
                    }
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

    public abstract Set<Class<?>> getEventClasses();

    private static class NoOp extends ListenersService {

        private static final NoOp INSTANCE = new NoOp();

        @Override
        public Set<Class<?>> getEventClasses() {
            return Collections.emptySet();
        }
    }
}
