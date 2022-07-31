package io.github.ulisse1996.jaorm.integration.test.mysql;

import com.zaxxer.hikari.HikariDataSource;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.integration.test.DatabaseInitializer;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.net.URL;

public class MySqlInitializer implements DatabaseInitializer {

    private static final Singleton<MySQLContainer<?>> INSTANCE = Singleton.instance();
    private static final Logger logger = LoggerFactory.getLogger(MySqlInitializer.class);

    @Override
    public void initDatabase() {
        logger.info("Starting Postgre");
        ensureInit();
        INSTANCE.get().start();
    }

    @Override
    public void destroyDatabase() {
        logger.info("Stopping Postgre");
        ensureInit();
        DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
        HikariDataSource d = (HikariDataSource) dataSource;
        d.close();
        INSTANCE.get().stop();
    }

    @Override
    public JdbcDatabaseContainer<?> getContainer() {
        ensureInit();
        return INSTANCE.get();
    }

    @Override
    public URL getSqlFileURL() {
        return MySqlInitializer.class.getResource("/init.sql");
    }

    private void ensureInit() {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(new MySQLContainer<>(DockerImageName.parse("mysql:5.7.34")));
            }
        }
    }
}
