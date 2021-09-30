package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.vendor.specific.Specific;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VendorSpecific {

    private static final Map<Class<? extends Specific>, Specific> SPECIFIC_MAP = new ConcurrentHashMap<>();

    private VendorSpecific() {}

    @SuppressWarnings("unchecked")
    public static synchronized <T extends Specific> T getSpecific(Class<T> klass) {
        if (SPECIFIC_MAP.containsKey(klass)) {
            return (T) SPECIFIC_MAP.get(klass);
        }

        Iterable<T> services = ServiceFinder.loadServices(klass);
        if (services.iterator().hasNext()) {
            SPECIFIC_MAP.put(klass, services.iterator().next());
            return (T) SPECIFIC_MAP.get(klass);
        }

        throw new IllegalArgumentException("Can't find specific for class " + klass);
    }
}
