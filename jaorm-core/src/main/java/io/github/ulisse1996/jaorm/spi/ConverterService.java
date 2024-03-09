package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.converter.ConverterPair;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultConverters;
import io.github.ulisse1996.jaorm.spi.provider.ConverterProvider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ConverterService {

    private static final Singleton<ConverterService> INSTANCE = Singleton.instance();
    private final Map<Class<?>, SqlAccessor> cache = new ConcurrentHashMap<>();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static ConverterService getInstance() {
        LOCK.lock();
        try {
            if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(INSTANCE.get().getConverters().keySet())) {
                INSTANCE.set(new DefaultConverters(ServiceFinder.loadServices(ConverterProvider.class)));
            }
        } finally {
            LOCK.unlock();
        }

        return INSTANCE.get();
    }

    @SuppressWarnings("unchecked")
    public <T, R> SqlAccessor findConverter(Class<R> klass) {
        if (cache.containsKey(klass)) {
            return cache.get(klass);
        }
        ConverterPair<T,R> converterPair = (ConverterPair<T, R>) getConverters().entrySet()
                .stream()
                .filter(el -> el.getKey().equals(klass))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
        if (converterPair == null) {
            return null;
        }
        SqlAccessor accessor = new SqlAccessor(
                klass,
                (rs, colName) -> {
                    SqlAccessor accessorBefore = SqlAccessor.find(converterPair.getOnSql());
                    Object getterValue = accessorBefore.getGetter().get(rs, colName);
                    return converterPair.getConverter().fromSql((T) getterValue);
                },
                (pr, index, value) -> {
                    Object setterValue;
                    if (converterPair.getOnSql().isInstance(value)) {
                        setterValue = value;
                    } else {
                        setterValue = converterPair.getConverter().toSql((R) value);
                    }
                    SqlAccessor found = Optional.ofNullable(setterValue)
                            .map(Object::getClass)
                            .map(SqlAccessor::find)
                            .orElse(SqlAccessor.NULL);
                    found.getSetter().set(pr, index, setterValue);
                }
        ) {};

        cache.put(klass, accessor);
        return accessor;
    }

    public abstract Map<Class<?>, ConverterPair<?,?>> getConverters(); // NOSONAR
}
