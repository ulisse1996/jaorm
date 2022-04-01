package io.github.ulisse1996.jaorm.tools.service.impl;

import io.github.ulisse1996.jaorm.DaoImplementation;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.logger.SqlJaormLogger;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.tools.cache.FileHashCache;
import io.github.ulisse1996.jaorm.tools.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.tools.exception.QueryValidationException;
import io.github.ulisse1996.jaorm.tools.logger.LogHolder;
import io.github.ulisse1996.jaorm.tools.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.tools.service.AbstractValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Collections;
import java.util.ServiceLoader;

@ExtendWith(MockitoExtension.class)
class ClasspathValidatorTest {

    @Mock private FileHashCache cache;
    private final JaormLogger logger = new SqlJaormLogger(ClasspathValidatorTest.class);
    @Mock private DelegatesService delegatesService;
    @Mock private QueriesService queries;
    @Mock private EntityDelegate<?> delegate;
    @Mock private ClassLoader classLoader;
    @Mock private URLClassLoader urlClassLoader;
    @Mock private ConnectionInfo connectionInfo;
    @Mock private ServiceLoader<DelegatesService> services;
    @Mock private ServiceLoader<QueriesService> queriesServices;
    @Mock private URL url;
    @Mock private EntityMapper<?> entityMapper;
    @Mock private Driver driver;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSetMetaData metaData;
    @Mock private DaoImplementation daoImpl;

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
    @SuppressWarnings("rawtypes")
    void should_skip_validation_for_same_hash_and_url_class_loader() throws IOException, NoSuchAlgorithmException, EntityValidationException, SQLException {
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            mk.when(() -> ServiceLoader.load(DelegatesService.class, urlClassLoader))
                    .thenReturn(services);
            Mockito.when(services.iterator())
                    .thenReturn(Collections.singletonList(delegatesService).iterator());
            Mockito.when(delegatesService.getDelegates())
                    .thenReturn(Collections.singletonMap(Object.class, () -> delegate));
            Mockito.when(urlClassLoader.getResource(Mockito.any()))
                    .thenReturn(url);
            Mockito.when(url.toString())
                    .thenReturn("url");
            Mockito.when(cache.getCurrentHash(Mockito.anyString())).thenReturn("HASH");
            Mockito.when(cache.calculateHash(Mockito.anyString())).thenReturn("HASH");
            ClasspathValidator validator = getValidator(false, urlClassLoader);
            validator.validateEntities();
            Mockito.verify(cache, Mockito.never())
                    .updateHash(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    void should_skip_validation_for_same_hash_and_normal_class_loader() throws IOException, NoSuchAlgorithmException, EntityValidationException, SQLException {
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            mk.when(() -> ServiceLoader.load(DelegatesService.class, classLoader))
                    .thenReturn(services);
            Mockito.when(services.iterator())
                    .thenReturn(Collections.singletonList(delegatesService).iterator());
            Mockito.when(delegatesService.getDelegates())
                    .thenReturn(Collections.singletonMap(Object.class, () -> delegate));
            Mockito.when(classLoader.getResource(Mockito.any()))
                    .thenReturn(url);
            Mockito.when(url.toString())
                    .thenReturn("url");
            Mockito.when(cache.getCurrentHash(Mockito.anyString())).thenReturn("HASH");
            Mockito.when(cache.calculateHash(Mockito.anyString())).thenReturn("HASH");
            ClasspathValidator validator = getValidator(false, classLoader);
            validator.validateEntities();
            Mockito.verify(cache, Mockito.never())
                    .updateHash(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    void should_update_cache() throws IOException, NoSuchAlgorithmException, EntityValidationException, SQLException {
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            mk.when(() -> ServiceLoader.load(DelegatesService.class, classLoader))
                    .thenReturn(services);
            Mockito.when(services.iterator())
                    .thenReturn(Collections.singletonList(delegatesService).iterator());
            Mockito.when(delegatesService.getDelegates())
                    .thenReturn(Collections.singletonMap(Object.class, () -> delegate));
            Mockito.when(classLoader.getResource(Mockito.any()))
                    .thenReturn(url);
            Mockito.when(url.toString())
                    .thenReturn("url");
            Mockito.when(delegate.getEntityMapper())
                    .then(invocation -> entityMapper);
            Mockito.when(entityMapper.getMappers())
                    .thenReturn(Collections.emptyList());
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(connectionInfo.getJdbcPassword()).thenReturn("pass");
            Mockito.when(connectionInfo.getJdbcUsername()).thenReturn("user");
            Mockito.when(connectionInfo.getJdbcUrl()).thenReturn("url");
            Mockito.when(driver.connect(Mockito.anyString(), Mockito.any()))
                    .thenReturn(connection);
            Mockito.when(connection.prepareStatement(Mockito.anyString()))
                    .thenReturn(preparedStatement);
            Mockito.when(preparedStatement.executeQuery())
                    .thenReturn(resultSet);
            Mockito.when(resultSet.getMetaData())
                    .thenReturn(metaData);
            Mockito.when(cache.getCurrentHash(Mockito.anyString())).thenReturn("HASH");
            Mockito.when(cache.calculateHash(Mockito.anyString())).thenReturn("HASH");
            ClasspathValidator validator = getValidator(true, classLoader);
            setDriver(validator);
            validator.validateEntities();
            Mockito.verify(cache, Mockito.times(1))
                    .updateHash(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    void should_skip_sql_validation_for_same_hash_and_normal_class_loader() throws IOException, NoSuchAlgorithmException, QueryValidationException {
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            mk.when(() -> ServiceLoader.load(QueriesService.class, classLoader))
                    .thenReturn(queriesServices);
            Mockito.when(queriesServices.iterator())
                    .thenReturn(Collections.singletonList(queries).iterator());
            Mockito.when(queries.getQueries())
                    .thenReturn(Collections.singletonMap(MyClass.class, daoImpl));
            Mockito.when(classLoader.getResource(Mockito.any()))
                    .thenReturn(url);
            Mockito.when(url.toString())
                    .thenReturn("url");
            Mockito.when(cache.getCurrentHash(Mockito.anyString())).thenReturn("HASH");
            Mockito.when(cache.calculateHash(Mockito.anyString())).thenReturn("HASH");
            ClasspathValidator validator = getValidator(false, classLoader);
            validator.validateQueries();
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    void should_validate_sql() throws IOException, NoSuchAlgorithmException, QueryValidationException {
        try (MockedStatic<ServiceLoader> mk = Mockito.mockStatic(ServiceLoader.class)) {
            mk.when(() -> ServiceLoader.load(QueriesService.class, urlClassLoader))
                    .thenReturn(queriesServices);
            Mockito.when(queriesServices.iterator())
                    .thenReturn(Collections.singletonList(queries).iterator());
            Mockito.when(queries.getQueries())
                    .thenReturn(Collections.singletonMap(MyClass.class, daoImpl));
            Mockito.when(urlClassLoader.getResource(Mockito.any()))
                    .thenReturn(url);
            Mockito.when(url.toString())
                    .thenReturn("url");
            Mockito.when(cache.getCurrentHash(Mockito.anyString())).thenReturn("HASH");
            Mockito.when(cache.calculateHash(Mockito.anyString())).thenReturn("HASH");
            ClasspathValidator validator = getValidator(true, urlClassLoader);
            validator.validateQueries();
            Mockito.verify(cache)
                    .updateHash(Mockito.anyString(), Mockito.anyString());
        }
    }

    private void setDriver(ClasspathValidator validator) {
        try {
            Field driverField = AbstractValidator.class.getDeclaredField("driver");
            driverField.setAccessible(true);
            driverField.set(validator, driver);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    private ClasspathValidator getValidator(boolean withSkip, ClassLoader cl) throws IOException {
        return new ClasspathValidator(
                cl,
                "root",
                connectionInfo,
                withSkip
        );
    }
}
