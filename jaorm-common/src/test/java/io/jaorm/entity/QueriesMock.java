package io.jaorm.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class QueriesMock extends QueriesService {

    @Override
    public Map<Class<?>, Supplier<?>> getQueries() {
        return Collections.singletonMap(String.class, () -> "1");
    }
}
