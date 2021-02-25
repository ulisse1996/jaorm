package io.jaorm.integration.test.spring;

import io.jaorm.entity.sql.DataSourceProvider;
import io.jaorm.integration.test.HSQLDBProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Field;

@SpringBootApplication(scanBasePackages = "io.jaorm.integration.test")
public class SpringTestApplication {

    public static final HSQLDBProvider PROVIDER = new HSQLDBProvider();

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }

    @Bean
    TransactionManager transactionManager() {
        try {
            PROVIDER.createFor(HSQLDBProvider.DatabaseType.ORACLE);
            Field instance = DataSourceProvider.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, PROVIDER);
            TransactionAwareDataSourceProxy proxy =
                    new TransactionAwareDataSourceProxy(PROVIDER.getDataSource());
            PROVIDER.set(proxy);
            return new DataSourceTransactionManager(proxy);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
