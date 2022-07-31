package io.github.ulisse1996.jaorm.integration.test.oracle;

import com.zaxxer.hikari.HikariDataSource;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.integration.test.DatabaseInitializer;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleInitializer implements DatabaseInitializer {

    private static final String SELECT_TABLES = "SELECT TABLE_NAME FROM USER_TABLES";

    private static final Singleton<OracleContainer> INSTANCE = Singleton.instance();
    private static final Logger logger = LoggerFactory.getLogger(OracleInitializer.class);

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
    public void beforeReset() throws SQLException {
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(SELECT_TABLES);
             ResultSet rs = pr.executeQuery()) {
            while (rs.next()) {
                execute(String.format("DROP TABLE \"%s\" CASCADE CONSTRAINTS", rs.getString(1)), true);
            }
        }
        execute("DROP SEQUENCE MY_PROG_SEQUENCE", false);
    }

    private void execute(String sql, boolean throwException) throws SQLException {
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
            PreparedStatement pr = connection.prepareStatement(sql)) {
            pr.execute();
        } catch (SQLException ex) {
            if (throwException) {
                throw ex;
            }
        }
    }

    @Override
    public URL getSqlFileURL() {
        return OracleInitializer.class.getResource("/init.sql");
    }

    private void ensureInit() {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:18.4.0-slim")));
            }
        }
    }
}
