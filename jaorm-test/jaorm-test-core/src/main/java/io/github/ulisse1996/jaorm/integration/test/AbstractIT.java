package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.MetricsService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@ExtendWith(ExceptionLogger.class)
public abstract class AbstractIT {

    private static final Logger logger = LoggerFactory.getLogger(AbstractIT.class);

    @BeforeAll
    public static void initDatabase() {
        ServiceFinder.loadService(DatabaseInitializer.class)
                .initDatabase();
    }

    @BeforeEach
    void resetQueryTracker() {
        MetricsService.getInstance().unwrap(ITMetricsTracker.class).reset();
    }

    @BeforeEach
    void initSql() throws Exception {
        try {
            DatabaseInitializer initializer = ServiceFinder.loadService(DatabaseInitializer.class);
            logger.info("Reset Database -- Start");
            initializer.beforeReset();
            URL url = initializer.getSqlFileURL();
            List<String> lines = Files.readAllLines(Paths.get(Objects.requireNonNull(url).toURI()));
            for (String line : lines) {
                line = line.replace(";", "");
                if (initializer.requiredSpecialExecute(line)) {
                    initializer.executeSpecial(line);
                } else {
                    execute(line);
                }
            }
            afterInit();
            logger.info("Reset Database -- End");
        } catch (Exception ex) {
            logger.error("", ex);
            throw ex;
        }
    }

    protected void afterInit() throws Exception {}

    protected void execute(String line) throws SQLException {
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(line)) {
            pr.executeUpdate();
        } catch (SQLException ex) {
            logger.info("Error on line {}", line);
            throw ex;
        }
    }

    @AfterAll
    public static void destroyDatabase() {
        ServiceFinder.loadService(DatabaseInitializer.class)
                .destroyDatabase();
    }

    protected void assertTotalInvocations(int times) {
        MetricsService.getInstance().unwrap(ITMetricsTracker.class).expectTotalInvocations(times);
    }
}
