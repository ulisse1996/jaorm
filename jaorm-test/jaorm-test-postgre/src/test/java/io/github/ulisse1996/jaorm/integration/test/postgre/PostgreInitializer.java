package io.github.ulisse1996.jaorm.integration.test.postgre;

import com.zaxxer.hikari.HikariDataSource;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.integration.test.DatabaseInitializer;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.net.URL;

public class PostgreInitializer implements DatabaseInitializer {

    private static final Singleton<PostgreSQLContainer<?>> INSTANCE = Singleton.instance();
    private static final Logger logger = LoggerFactory.getLogger(PostgreInitializer.class);

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
        return PostgreInitializer.class.getResource("/init.sql");
    }

    private void ensureInit() {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4")));
            }
        }
    }
}
