package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.GenerationInfo;
import io.github.ulisse1996.jaorm.spi.combined.CombinedGenerators;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.sql.SQLException;
import java.util.*;

public abstract class GeneratorsService {

    private static final Singleton<GeneratorsService> INSTANCE = Singleton.instance();

    public static synchronized GeneratorsService getInstance() {
        if (!INSTANCE.isPresent()) {
            try {
                Iterator<GeneratorsService> iterator = ServiceFinder.loadServices(GeneratorsService.class).iterator();
                if (iterator.hasNext()) {
                    List<GeneratorsService> services = new ArrayList<>();
                    while (iterator.hasNext()) {
                        services.add(iterator.next());
                    }
                    if (services.size() == 1) {
                        INSTANCE.set(services.get(0));
                    } else {
                        INSTANCE.set(new CombinedGenerators(services));
                    }
                } else {
                    INSTANCE.set(NoOp.INSTANCE);
                }
            } catch (ServiceConfigurationError ex) {
                INSTANCE.set(NoOp.INSTANCE);
            }
        }

        return INSTANCE.get();
    }

    public boolean canGenerateValue(Class<?> klass, String columnName) {
        List<GenerationInfo> infos = getGenerated()
                .get(klass);
        return infos != null && infos.stream()
                .anyMatch(i -> i.getColumnName().equalsIgnoreCase(columnName));
    }

    public <T> T generate(Class<?> klass, String columnName, Class<?> columnClass) throws SQLException {
        Optional<GenerationInfo> info = getGenerated()
                .get(klass)
                .stream()
                .filter(i -> i.getColumnName().equalsIgnoreCase(columnName))
                .findFirst();
        if (!info.isPresent()) {
            throw new IllegalArgumentException("Can't process generation");
        }

        return info.get().generate(klass, columnClass);
    }

    public boolean needGeneration(Class<?> entityClass) {
        return getGenerated().get(entityClass) != null;
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
