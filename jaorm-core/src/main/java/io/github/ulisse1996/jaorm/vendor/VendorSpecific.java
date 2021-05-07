package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.vendor.specific.Specific;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class VendorSpecific {

    private static final Map<Class<? extends Specific>, Specific> SPECIFIC_MAP = new ConcurrentHashMap<>();

    private VendorSpecific() {}

    @SuppressWarnings("unchecked")
    public static synchronized <T extends Specific> T getSpecific(Class<T> klass) {
        if (SPECIFIC_MAP.containsKey(klass)) {
            return (T) SPECIFIC_MAP.get(klass);
        }

        // We search for specific that is also valid but stored with a different specific class
        Optional<Specific> stored = SPECIFIC_MAP.values()
                .stream()
                .filter(v -> klass.isAssignableFrom(v.getClass()))
                .findFirst();
        if (stored.isPresent()) {
            return (T) stored.get();
        }

        for (T val : ServiceFinder.loadServices(klass)) {
            if (val.supportSpecific()) {
                SPECIFIC_MAP.put(klass, val);
                return val;
            }
        }

        throw new IllegalArgumentException("Can't find specific for class " + klass);
    }
}
