package io.jaorm.spi;

import io.jaorm.ServiceFinder;
import io.jaorm.spi.common.Singleton;

import java.util.Iterator;

public interface DslService {

    Singleton<DslService> INSTANCE = Singleton.instance();

    static DslService getInstance() {
        if (!INSTANCE.isPresent()) {
            Iterator<DslService> iterator = ServiceFinder.loadServices(DslService.class)
                    .iterator();
            if (iterator.hasNext()) {
                INSTANCE.set(iterator.next());
            } else {
                INSTANCE.set(NoOpModification.INSTANCE);
            }
        }

        return INSTANCE.get();
    }

    boolean isSupported();

    class NoOpModification implements DslService {

        private static final NoOpModification INSTANCE = new NoOpModification();

        @Override
        public boolean isSupported() {
            return false;
        }
    }
}
