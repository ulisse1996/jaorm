package io.jaorm.integration.test;

import io.jaorm.cache.EntityCache;
import io.jaorm.cache.StandardConfiguration;
import io.jaorm.entity.sql.DataSourceProvider;
import io.jaorm.integration.test.entity.Role;
import io.jaorm.spi.CacheService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractIT {

    private final boolean skipSetup;

    public AbstractIT() {
        this(false);
    }

    public AbstractIT(boolean skipSetup) {
        this.skipSetup = skipSetup;
    }


    protected void setProvider(HSQLDBProvider provider) {
        try {
            Field instance = DataSourceProvider.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, provider);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @BeforeEach
    public void setup() {
        if (!this.skipSetup) {
            try {
                Field instance = DataSourceProvider.class.getDeclaredField("instance");
                instance.setAccessible(true);
                instance.set(null, null);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        fillCache();
    }

    private static void fillCache() {
        Map<Class<?>, EntityCache<?>> cacheMap = CacheService.getCurrent().getCaches();
        Stream.of(Role.class)
                .forEach(c -> cacheMap.put(c, EntityCache.fromConfiguration(c, new StandardConfiguration())));
    }

    protected void setDataSource(HSQLDBProvider.DatabaseType type, String initSql) {
        DataSourceProvider current = DataSourceProvider.getCurrent();
        ((HSQLDBProvider)current).createFor(type);
        createDB(initSql);
    }

    protected void createDB(String initSql) {
        List<String> strings = readFile(initSql);
        strings.add(0, "DROP SCHEMA PUBLIC CASCADE");
        prepareDb(strings);
    }

    protected void prepareDb(List<String> statements) {
        for (String s : statements) {
            try (Connection cn = DataSourceProvider.getCurrent().getConnection();
                 PreparedStatement pr = cn.prepareStatement(s)){
                pr.execute();
            } catch (SQLException ex) {
                Assertions.fail(ex);
            }
        }
    }

    protected List<String> readFile(String initSql) {
        try {
            return Files.readAllLines(Paths.get(JaormDSLIT.class.getResource("/inits/" + initSql).toURI()));
        } catch (Exception ex) {
            Assertions.fail(ex);
            return Collections.emptyList();
        }
    }

    public static Stream<Arguments> getSqlTests() {
        return Stream.of(
                Arguments.arguments(HSQLDBProvider.DatabaseType.ORACLE, "init.sql"),
                Arguments.arguments(HSQLDBProvider.DatabaseType.DB2, "init.sql"),
                Arguments.arguments(HSQLDBProvider.DatabaseType.MS_SQLSERVER, "init.sql"),
                Arguments.arguments(HSQLDBProvider.DatabaseType.MYSQL, "init.sql"),
                Arguments.arguments(HSQLDBProvider.DatabaseType.POSTGRE, "init.sql")
        );
    }
}
