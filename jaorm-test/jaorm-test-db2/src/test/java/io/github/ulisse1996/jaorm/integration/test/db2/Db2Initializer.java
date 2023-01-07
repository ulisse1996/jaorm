package io.github.ulisse1996.jaorm.integration.test.db2;

import com.zaxxer.hikari.HikariDataSource;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.integration.test.DatabaseInitializer;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Db2Initializer implements DatabaseInitializer {

    private static final Singleton<Db2Container> INSTANCE = Singleton.instance();
    private static final Logger logger = LoggerFactory.getLogger(Db2Initializer.class);

    @Override
    public void initDatabase() {
        logger.info("Starting Db2");
        ensureInit();
        INSTANCE.get().start();
    }

    @Override
    public void destroyDatabase() {
        logger.info("Stopping Db2");
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
    public void beforeReset() {
    }

    @Override
    public boolean requiredSpecialExecute(String line) {
        return line.equalsIgnoreCase("--");
    }

    @Override
    public void executeSpecial(String line) throws SQLException {
        List<String> sql = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement("Select CONCAT('DROP TABLE ', tabname) from syscat.tables where owner='DB2INST1'");
             ResultSet rs = pr.executeQuery()) {
            while (rs.next()) {
                sql.add(rs.getString(1));
            }
        }
        for (String s : sql) {
            try (Connection connection = DataSourceProvider.getCurrent().getConnection();
                PreparedStatement pr = connection.prepareStatement(s)) {
                pr.execute();
            }
        }
    }

    @Override
    public URL getSqlFileURL() {
        return Db2Initializer.class.getResource("/init.sql");
    }

    private void ensureInit() {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(
                        new Db2Container(DockerImageName.parse("ibmcom/db2:11.5.0.0a"))
                                .withUrlParam("loglevel", "2")
                );
                INSTANCE.get().acceptLicense();
            }
        }
    }
}
