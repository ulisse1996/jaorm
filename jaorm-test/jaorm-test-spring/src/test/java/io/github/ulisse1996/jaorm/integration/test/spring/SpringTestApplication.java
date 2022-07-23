package io.github.ulisse1996.jaorm.integration.test.spring;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.TransactionManager;

@org.springframework.boot.autoconfigure.SpringBootApplication(scanBasePackages = "io.github.ulisse1996.jaorm.integration.test")
public class SpringTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }

    @Bean
    TransactionManager transactionManager() {
        TransactionAwareDataSourceProxy proxy = (TransactionAwareDataSourceProxy) DataSourceProvider.getCurrent().getDataSource();
        return new DataSourceTransactionManager(proxy);
    }
}
