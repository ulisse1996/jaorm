package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public abstract class MultiSchemaDatasourceProvider {

    private static final Singleton<MultiSchemaDatasourceProvider> INSTANCE = Singleton.instance();
    private static final ThreadLocal<Map<Class<?>, DataSourceProvider>> TRANSACTION_INSTANCES = ThreadLocal.withInitial(HashMap::new);

    public static synchronized MultiSchemaDatasourceProvider getInstance() {
        if (INSTANCE.isPresent()) {
            return INSTANCE.get();
        }

        // We don't catch this call because this provider is mandatory !
        INSTANCE.set(MultiSchemaDatasourceProvider.getInstance());

        return INSTANCE.get();
    }

    public static synchronized DataSourceProvider getCurrentDelegate(Class<?> klass) {
        return TRANSACTION_INSTANCES.get().get(klass);
    }

    public static synchronized void setDelegate(Class<?> klass, DataSourceProvider provider) {
        if (provider == null) {
            TRANSACTION_INSTANCES.get().remove(klass);
        } else {
            TRANSACTION_INSTANCES.get().put(klass, provider);
        }
    }

    public static synchronized void clearDelegates() {
        TRANSACTION_INSTANCES.get().clear();
        TRANSACTION_INSTANCES.remove();
    }

    public abstract DataSource getDatasource(Class<?> delegateClass);
}
