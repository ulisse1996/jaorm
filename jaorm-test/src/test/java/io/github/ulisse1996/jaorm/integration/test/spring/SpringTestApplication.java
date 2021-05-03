package io.github.ulisse1996.jaorm.integration.test.spring;

import io.github.ulisse1996.jaorm.integration.test.HSQLDBProvider;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.TransactionManager;

import java.lang.reflect.Field;

@SpringBootApplication(scanBasePackages = "io.github.ulisse1996.jaorm.integration.test")
public class SpringTestApplication {

    public static final HSQLDBProvider PROVIDER = new HSQLDBProvider();

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }

    @Bean
    @SuppressWarnings("unchecked")
    TransactionManager transactionManager() {
        try {
            PROVIDER.createFor(HSQLDBProvider.DatabaseType.ORACLE);
            Field instance = DataSourceProvider.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<DataSourceProvider> singleton = (Singleton<DataSourceProvider>) instance.get(null);
            singleton.set(PROVIDER);
            TransactionAwareDataSourceProxy proxy =
                    new TransactionAwareDataSourceProxy(PROVIDER.getDataSource());
            PROVIDER.set(proxy);
            return new DataSourceTransactionManager(proxy);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
