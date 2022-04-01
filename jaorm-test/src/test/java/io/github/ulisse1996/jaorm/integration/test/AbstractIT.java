package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.integration.test.util.ExceptionLogger;
import io.github.ulisse1996.jaorm.spi.CacheService;
import io.github.ulisse1996.jaorm.spi.TransactionManager;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@ExtendWith(ExceptionLogger.class)
public abstract class AbstractIT {

    private final boolean skipSetup;
    private final boolean withTransaction;

    public AbstractIT() {
        this(false);
    }

    public AbstractIT(boolean skipSetup) {
        this(skipSetup, false);
    }

    public AbstractIT(boolean skipSetup, boolean withTransaction) {
        this.skipSetup = skipSetup;
        this.withTransaction = withTransaction;
    }

    @SuppressWarnings("unchecked")
    protected void setProvider(HSQLDBProvider provider) {
        try {
            Field instance = DataSourceProvider.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<DataSourceProvider> singleton = (Singleton<DataSourceProvider>) instance.get(null);
            singleton.set(provider);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() {
        if (!this.skipSetup) {
            try {

                // Reset datasource
                Field instance = DataSourceProvider.class.getDeclaredField("INSTANCE");
                instance.setAccessible(true);
                Singleton<DataSourceProvider> singleton = (Singleton<DataSourceProvider>) instance.get(null);
                singleton.set(null);

                // Reset cache
                instance = CacheService.class.getDeclaredField("INSTANCE");
                instance.setAccessible(true);
                Singleton<CacheService> cacheServiceSingleton = (Singleton<CacheService>) instance.get(null);
                cacheServiceSingleton.set(null);

                // Reset transactional service
                instance = TransactionManager.class.getDeclaredField("INSTANCE");
                instance.setAccessible(true);
                Singleton<TransactionManager> managerSingleton = (Singleton<TransactionManager>) instance.get(null);
                managerSingleton.set(null);
                if (!withTransaction) {
                    managerSingleton.set(TransactionManager.NoOpTransactionManager.INSTANCE);
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    protected void setDataSource(HSQLDBProvider.DatabaseType type, String initSql) {
        DataSourceProvider current = DataSourceProvider.getCurrent();
        ((HSQLDBProvider)current).createFor(type);
        createDB(initSql);
    }

    protected String getLastExecuted() {
        List<String> executedSql = HSQLDBProvider.getExecutedSql();
        return executedSql.get(executedSql.size() - 1);
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
            return Files.readAllLines(Paths.get(Objects.requireNonNull(QueryBuilderIT.class.getResource("/inits/" + initSql)).toURI()));
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
