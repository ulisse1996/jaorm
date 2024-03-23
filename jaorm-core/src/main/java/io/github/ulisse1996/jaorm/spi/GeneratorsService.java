package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.GenerationInfo;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultGenerators;
import io.github.ulisse1996.jaorm.spi.provider.GeneratorProvider;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public abstract class GeneratorsService {

    private static final Singleton<GeneratorsService> INSTANCE = Singleton.instance();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static GeneratorsService getInstance() {
        LOCK.lock();
        try {
            if (!INSTANCE.isPresent() || FrameworkIntegrationService.isReloadRequired(INSTANCE.get().getGenerated().keySet())) {
                Iterable<GeneratorProvider> iterable = ServiceFinder.loadServices(GeneratorProvider.class);
                if (iterable.iterator().hasNext()) {
                    INSTANCE.set(new DefaultGenerators(iterable));
                } else {
                    INSTANCE.set(NoOp.INSTANCE);
                }
            }
        } finally {
            LOCK.unlock();
        }

        return INSTANCE.get();
    }

    public boolean canGenerateValue(Class<?> klass, String columnName) {
        List<GenerationInfo> infos = getGenerated()
                .entrySet()
                .stream()
                .filter(el -> el.getKey().equals(klass))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
        return infos != null && infos.stream()
                .anyMatch(i -> i.getColumnName().equalsIgnoreCase(columnName));
    }

    public <T> T generate(Class<?> klass, String columnName, Class<?> columnClass) throws SQLException {
        Optional<GenerationInfo> info = getGenerated()
                .entrySet()
                .stream()
                .filter(el -> el.getKey().equals(klass))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(i -> i.getColumnName().equalsIgnoreCase(columnName))
                .findFirst();
        if (info.isEmpty()) {
            throw new IllegalArgumentException("Can't process generation");
        }

        return info.get().generate(klass, columnClass);
    }

    public boolean needGeneration(Class<?> entityClass) {
        return getGenerated()
                .entrySet()
                .stream()
                .anyMatch(el -> el.getKey().equals(entityClass));
    }

    public abstract Map<Class<?>, List<GenerationInfo>> getGenerated();

    protected static final class NoOp extends GeneratorsService {

        private static final NoOp INSTANCE = new NoOp();

        @Override
        public Map<Class<?>, List<GenerationInfo>> getGenerated() {
            return Collections.emptyMap();
        }
    }
}
