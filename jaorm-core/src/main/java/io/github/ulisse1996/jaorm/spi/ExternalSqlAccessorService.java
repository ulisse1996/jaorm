package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultSqlAccessors;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ExternalSqlAccessorService {

    private static final Singleton<ExternalSqlAccessorService> INSTANCE = Singleton.instance();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static ExternalSqlAccessorService getInstance() {
        LOCK.lock();
        try {
            if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(Collections.singleton(INSTANCE.get().getClass()))) {
                Iterable<SqlAccessor> iterable = ServiceFinder.loadServices(SqlAccessor.class);
                if (iterable.iterator().hasNext()) {
                    INSTANCE.set(new DefaultSqlAccessors(iterable));
                } else {
                    INSTANCE.set(ExternalSqlAccessorService.NoOp.INSTANCE);
                }
            }
        } finally {
            LOCK.unlock();
        }

        return INSTANCE.get();
    }

    public abstract Set<SqlAccessor> getAccessors();

    static class NoOp extends ExternalSqlAccessorService {

        private static final ExternalSqlAccessorService INSTANCE = new NoOp();

        @Override
        public Set<SqlAccessor> getAccessors() {
            return Collections.emptySet();
        }
    }
}
