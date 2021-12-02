package io.github.ulisse1996.jaorm.tools.service.impl;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.tools.cache.FileHashCache;
import io.github.ulisse1996.jaorm.tools.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.tools.exception.QueryValidationException;
import io.github.ulisse1996.jaorm.tools.logger.LogHolder;
import io.github.ulisse1996.jaorm.tools.model.ConnectionInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

@ExtendWith(MockitoExtension.class)
class SourceValidatorTest {

    @Mock private FileHashCache cache;
    @Mock private ConnectionInfo connectionInfo;
    @Mock private JaormLogger logger;
    @Mock private Driver mockDriver;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSetMetaData metaData;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() {
        try {
            Field instance = FileHashCache.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Singleton<FileHashCache> singleton = (Singleton<FileHashCache>) instance.get(null);
            singleton.set(cache);
            LogHolder.set(logger);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @AfterEach
    void destroy() {
        LogHolder.destroy();
    }

    @Test
    void should_not_validate_entity_klass_for_same_hash() throws IOException, NoSuchAlgorithmException, EntityValidationException, SQLException, URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/src")).toURI());
        Path tempFile = createJar();
        try {
            SourceValidator validator = new SourceValidator(
                    Collections.singletonList(path.toString()),
                    "",
                    Collections.singletonList(tempFile.toFile()),
                    connectionInfo,
                    false
            );
            Mockito.when(cache.calculateHash(Mockito.anyString()))
                    .thenReturn("MOCK");
            Mockito.when(cache.getCurrentHash(Mockito.anyString()))
                    .thenReturn("MOCK");
            validator.validateEntities();
            Mockito.verify(cache, Mockito.never())
                    .updateHash(Mockito.anyString(), Mockito.anyString());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void should_throw_exception_for_missing_driver_class() throws IOException, NoSuchAlgorithmException, URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/src")).toURI());
        SourceValidator validator = new SourceValidator(
                Collections.singletonList(path.toString()),
                "",
                Collections.emptyList(),
                connectionInfo,
                true
        );
        Mockito.when(cache.calculateHash(Mockito.anyString()))
                .thenReturn("MOCK");
        Mockito.when(cache.getCurrentHash(Mockito.anyString()))
                .thenReturn("MOCK");
        Mockito.when(connectionInfo.getJdbcDriver())
                .thenReturn("wrong.class");
        try {
            validator.validateEntities();
        } catch (SQLException | EntityValidationException ex) {
            Assertions.assertTrue(ex.getCause() instanceof ClassNotFoundException);
        }
    }

    @Test
    void should_use_custom_driver_for_validation() throws IOException, NoSuchAlgorithmException, URISyntaxException, SQLException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/src")).toURI());
        SourceValidator validator = new SourceValidator(
                Collections.singletonList(path.toString()),
                "",
                Collections.emptyList(),
                connectionInfo,
                true
        );
        Mockito.when(connectionInfo.getJdbcDriver())
                .thenReturn(MyDriver.class.getName());
        Mockito.when(cache.calculateHash(Mockito.anyString()))
                .thenReturn("HASH");
        Mockito.when(cache.getCurrentHash(Mockito.anyString()))
                .thenReturn("NOT_HASH");
        Mockito.when(connectionInfo.getJdbcUsername())
                .thenReturn("Username");
        Mockito.when(connectionInfo.getJdbcPassword())
                .thenReturn("Pass");
        Mockito.when(connectionInfo.getJdbcUrl())
                .thenReturn("url");
        Mockito.when(MyDriver.CONNECTION.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);
        Mockito.when(metaData.getColumnCount())
                .thenReturn(2);
        Mockito.when(metaData.getColumnName(1))
                .thenReturn("COL1");
        Mockito.when(metaData.getColumnType(1))
                .thenReturn(JDBCType.INTEGER.getVendorTypeNumber());
        Mockito.when(metaData.getColumnName(2))
                .thenReturn("COL2");
        Mockito.when(metaData.getColumnType(2))
                .thenReturn(JDBCType.VARCHAR.getVendorTypeNumber());
        Assertions.assertDoesNotThrow(validator::validateEntities);
    }

    @Test
    void should_not_find_column_for_class_validation() throws IOException, URISyntaxException, NoSuchAlgorithmException, SQLException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/src")).toURI());
        SourceValidator validator = new SourceValidator(
                Collections.singletonList(path.toString()),
                "",
                Collections.emptyList(),
                connectionInfo,
                false
        );
        setDriver(validator);
        mockMetadata();
        Mockito.when(metaData.getColumnCount())
                .thenReturn(0);
        try {
            validator.validateEntities();
            Assertions.fail("Should throw ValidationException !");
        } catch (EntityValidationException ex) {
            Assertions.assertNotNull(ex.getMessage());
        }
    }

    @Test
    void should_not_find_compatible_column_type_for_class_validation() throws IOException, URISyntaxException, NoSuchAlgorithmException, SQLException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/src")).toURI());
        SourceValidator validator = new SourceValidator(
                Collections.singletonList(path.toString()),
                "",
                Collections.emptyList(),
                connectionInfo,
                false
        );
        setDriver(validator);
        mockMetadata();
        Mockito.when(metaData.getColumnCount())
                .thenReturn(1);
        Mockito.when(metaData.getColumnName(1))
                .thenReturn("COL1");
        Mockito.when(metaData.getColumnType(1))
                .thenReturn(JDBCType.VARCHAR.getVendorTypeNumber());
        try {
            validator.validateEntities();
            Assertions.fail("Should throw ValidationException !");
        } catch (EntityValidationException ex) {
            Assertions.assertNotNull(ex.getMessage());
        }
    }

    @Test
    void should_validate_entity_class() throws IOException, URISyntaxException, NoSuchAlgorithmException, SQLException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/src")).toURI());
        SourceValidator validator = new SourceValidator(
                Collections.singletonList(path.toString()),
                "",
                Collections.emptyList(),
                connectionInfo,
                false
        );
        setDriver(validator);
        mockMetadata();
        Mockito.when(metaData.getColumnCount())
                .thenReturn(2);
        Mockito.when(metaData.getColumnName(1))
                .thenReturn("COL1");
        Mockito.when(metaData.getColumnType(1))
                .thenReturn(JDBCType.INTEGER.getVendorTypeNumber());
        Mockito.when(metaData.getColumnName(2))
                .thenReturn("COL2");
        Mockito.when(metaData.getColumnType(2))
                .thenReturn(JDBCType.VARCHAR.getVendorTypeNumber());
        Assertions.assertDoesNotThrow(validator::validateEntities);
    }

    private void mockMetadata() throws IOException, NoSuchAlgorithmException, SQLException {
        Mockito.when(cache.calculateHash(Mockito.anyString()))
                .thenReturn("HASH");
        Mockito.when(cache.getCurrentHash(Mockito.anyString()))
                .thenReturn("NOT_HASH");
        Mockito.when(connectionInfo.getJdbcUsername())
                .thenReturn("Username");
        Mockito.when(connectionInfo.getJdbcPassword())
                .thenReturn("Pass");
        Mockito.when(connectionInfo.getJdbcUrl())
                .thenReturn("url");
        Mockito.when(mockDriver.connect(Mockito.anyString(), Mockito.any()))
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);
    }

    private void setDriver(SourceValidator validator) {
        try {
            Field driver = validator.getClass().getSuperclass().getDeclaredField("driver");
            driver.setAccessible(true);
            driver.set(validator, mockDriver);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_not_validate_klass_for_same_hash() throws IOException, URISyntaxException, QueryValidationException, NoSuchAlgorithmException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/src")).toURI());
        Path tempFile = createJar();
        try {
            SourceValidator validator = new SourceValidator(
                    Collections.singletonList(path.toString()),
                    "",
                    Collections.singletonList(tempFile.toFile()),
                    connectionInfo,
                    false
            );
            Mockito.when(cache.calculateHash(Mockito.anyString()))
                    .thenReturn("MOCK");
            Mockito.when(cache.getCurrentHash(Mockito.anyString()))
                    .thenReturn("MOCK");
            validator.validateQueries();
            Mockito.verify(cache, Mockito.never())
                    .updateHash(Mockito.anyString(), Mockito.anyString());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void should_validate_sql() throws IOException, URISyntaxException, NoSuchAlgorithmException, SQLException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/src")).toURI());
        SourceValidator validator = new SourceValidator(
                Collections.singletonList(path.toString()),
                "",
                Collections.emptyList(),
                connectionInfo,
                true
        );
        Mockito.when(cache.calculateHash(Mockito.anyString()))
                .thenReturn("MOCK");
        Mockito.when(cache.getCurrentHash(Mockito.anyString()))
                .thenReturn("MOCK");
        Mockito.when(connectionInfo.getJdbcUsername())
                .thenReturn("user");
        Mockito.when(connectionInfo.getJdbcPassword())
                .thenReturn("Pass");
        Mockito.when(connectionInfo.getJdbcUrl())
                .thenReturn("url");
        setDriver(validator);
        Mockito.when(mockDriver.connect(Mockito.anyString(), Mockito.any()))
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Assertions.assertDoesNotThrow(validator::validateQueries);
    }

    @Test
    void should_not_validate_sql_for_missing_strategy() throws IOException, URISyntaxException, NoSuchAlgorithmException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/fails/failsNoStrategy")).toURI());
        SourceValidator validator = new SourceValidator(
                Collections.singletonList(path.toString()),
                "",
                Collections.emptyList(),
                connectionInfo,
                true
        );
        Mockito.when(cache.calculateHash(Mockito.anyString()))
                .thenReturn("MOCK");
        Mockito.when(cache.getCurrentHash(Mockito.anyString()))
                .thenReturn("MOCK");
        Assertions.assertDoesNotThrow(validator::validateQueries);
        Mockito.verify(logger)
                .warn(Mockito.any());
    }

    @Test
    void should_not_validate_sql_for_bad_sql() throws IOException, URISyntaxException, NoSuchAlgorithmException, SQLException {
        Path path = Paths.get(Objects.requireNonNull(SourceValidatorTest.class.getResource("/fails/failsSql")).toURI());
        SourceValidator validator = new SourceValidator(
                Collections.singletonList(path.toString()),
                "",
                Collections.emptyList(),
                connectionInfo,
                true
        );
        Mockito.when(cache.calculateHash(Mockito.anyString()))
                .thenReturn("MOCK");
        Mockito.when(cache.getCurrentHash(Mockito.anyString()))
                .thenReturn("MOCK");
        Mockito.when(connectionInfo.getJdbcUsername())
                .thenReturn("user");
        Mockito.when(connectionInfo.getJdbcPassword())
                .thenReturn("Pass");
        Mockito.when(connectionInfo.getJdbcUrl())
                .thenReturn("url");
        setDriver(validator);
        Mockito.when(mockDriver.connect(Mockito.anyString(), Mockito.any()))
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenThrow(SQLException.class);
        Assertions.assertThrows(QueryValidationException.class, validator::validateQueries);
    }

    private Path createJar() throws IOException {
        Path tempFile = Files.createTempFile("test-jar", ".jar");
        try (JarOutputStream os = new JarOutputStream(Files.newOutputStream(tempFile))) {
            ZipEntry entry = new ZipEntry("test-file.class");
            os.putNextEntry(entry);
        }

        return tempFile;
    }

    public static class MyDriver implements Driver {

        private static final Connection CONNECTION = Mockito.mock(Connection.class);

        @Override
        public Connection connect(String url, Properties info) {
            return CONNECTION;
        }

        @Override
        public boolean acceptsURL(String url) {
            return false;
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 0;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() {
            return null;
        }
    }
}
