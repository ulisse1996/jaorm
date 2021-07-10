package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.converter.ConverterPair;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConverterService {

    private static final Singleton<ConverterService> INSTANCE = Singleton.instance();
    private final Map<Class<?>, SqlAccessor> cache = new ConcurrentHashMap<>();

    public static synchronized ConverterService getInstance() {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(ServiceFinder.loadService(ConverterService.class));
        }

        return INSTANCE.get();
    }

    @SuppressWarnings("unchecked")
    public <T, R> SqlAccessor findConverter(Class<R> klass) {
        if (cache.containsKey(klass)) {
            return cache.get(klass);
        }
        ConverterPair<T,R> converterPair = (ConverterPair<T, R>) getConverters().get(klass);
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

    protected abstract Map<Class<?>, ConverterPair<?,?>> getConverters(); // NOSONAR
}
