package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;

public interface DatabaseInitializer {

    void initDatabase();
    void destroyDatabase();
    JdbcDatabaseContainer<?> getContainer(); //NOSONAR
    URL getSqlFileURL();
    default void beforeReset() throws SQLException {}

    @SuppressWarnings("unchecked")
    default void resetProvider() {
        try {
            Field instance = DataSourceProvider.class.getDeclaredField("INSTANCE");
            Field transactionInstance = DataSourceProvider.class.getDeclaredField("TRANSACTION_INSTANCE");
            instance.setAccessible(true); //NOSONAR
            transactionInstance.setAccessible(true); //NOSONAR
            Singleton<DataSourceProvider> singleton = (Singleton<DataSourceProvider>) instance.get(null);
            ThreadLocal<DataSourceProvider> threadLocal = (ThreadLocal<DataSourceProvider>) transactionInstance.get(null);
            singleton.set(null);
            threadLocal.set(null);
        } catch (Exception ex) {
            Assertions.fail();
        }
    }
}
