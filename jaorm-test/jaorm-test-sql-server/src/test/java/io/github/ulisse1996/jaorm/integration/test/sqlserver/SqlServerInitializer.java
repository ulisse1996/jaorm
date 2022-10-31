package io.github.ulisse1996.jaorm.integration.test.sqlserver;

import com.zaxxer.hikari.HikariDataSource;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.integration.test.DatabaseInitializer;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqlServerInitializer implements DatabaseInitializer {

    private static final Singleton<MSSQLServerContainer<?>> INSTANCE = Singleton.instance();
    private static final Logger logger = LoggerFactory.getLogger(SqlServerInitializer.class);

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
        List<String> toBeDropped = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement("SELECT name FROM sys.Tables");
             ResultSet rs = pr.executeQuery()) {
            while (rs.next()) {
                boolean executed = execute(String.format("DROP TABLE %s", rs.getString(1)), false);
                if (!executed) {
                    toBeDropped.add(rs.getString(1));
                }
            }
        }

        // Drop remaining tables with foreign keys
        for (String s : toBeDropped) {
            execute(String.format("DROP TABLE %s", s), true);
        }

        execute("DROP SEQUENCE MY_PROG_SEQUENCE", false);
    }

    private boolean execute(String sql, boolean withThrow) throws SQLException {
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
            PreparedStatement pr = connection.prepareStatement(sql)) {
            pr.execute();
            return true;
        } catch (SQLException ex) {
            if (withThrow) {
                throw ex;
            } else {
                return false;
            }
        }
    }

    @Override
    public URL getSqlFileURL() {
        return SqlServerInitializer.class.getResource("/init.sql");
    }

    private void ensureInit() {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:latest")));
                INSTANCE.get().acceptLicense();
            }
        }
    }
}
