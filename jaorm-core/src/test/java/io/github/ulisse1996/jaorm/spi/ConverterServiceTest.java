package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ConverterMock;
import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.spi.impl.DefaultConverters;
import io.github.ulisse1996.jaorm.spi.provider.ConverterProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

class ConverterServiceTest {

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {
        try {
            Field field = ConverterService.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Singleton<ConverterService> instance = (Singleton<ConverterService>) field.get(null);
            instance.set(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_not_find_accessor() {
        ConverterMock testSubject = new ConverterMock();
        Assertions.assertNull(testSubject.findConverter(String.class));
    }

    @Test
    void should_find_accessor() {
        ConverterMock testSubject = new ConverterMock();
        Assertions.assertNotNull(testSubject.findConverter(Boolean.class));
    }

    @Test
    void should_find_accessor_with_cache() {
        ConverterMock testSubject = new ConverterMock();
        SqlAccessor found = testSubject.findConverter(Boolean.class);
        Assertions.assertNotNull(found);
        Assertions.assertSame(found, testSubject.findConverter(Boolean.class));
    }

    @Test
    void should_convert_and_get_and_set_converted_values() throws SQLException {
        ConverterMock testSubject = new ConverterMock();
        SqlAccessor found = testSubject.findConverter(Boolean.class);
        PreparedStatement pr = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        found.getSetter().set(pr, 1, true);
        Object value = found.getGetter().get(rs, "COL");
        Assertions.assertTrue(value instanceof Boolean);
        Mockito.verify(pr).setInt(1, 1);
        Mockito.verify(rs).getInt("COL");
    }

    @Test
    void should_use_converted_value_and_not_apply_conversion() throws SQLException {
        ConverterMock testSubject = new ConverterMock();
        SqlAccessor found = testSubject.findConverter(Boolean.class);
        PreparedStatement pr = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        found.getSetter().set(pr, 1, 1);
        Object value = found.getGetter().get(rs, "COL");
        Assertions.assertTrue(value instanceof Boolean);
        Mockito.verify(pr).setInt(1, 1);
        Mockito.verify(rs).getInt("COL");
    }

    @Test
    void should_return_default_impl() {
        ConverterProvider provider = Mockito.mock(ConverterProvider.class);
        ValueConverter<?, ?> c = Mockito.mock(ValueConverter.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(ConverterProvider.class))
                    .thenReturn(Collections.singletonList(provider));
            Mockito.when(provider.to()).then(invocation -> Object.class);
            Mockito.when(provider.from()).then(invocation -> Object.class);
            Mockito.when(provider.converter()).then(invocation -> c);

            ConverterService service = ConverterService.getInstance();

            Assertions.assertTrue(service instanceof DefaultConverters);
        }
    }

    @Test
    void should_return_same_instance() {
        ConverterProvider provider = Mockito.mock(ConverterProvider.class);
        ValueConverter<?, ?> c = Mockito.mock(ValueConverter.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(ConverterProvider.class))
                    .thenReturn(Collections.singletonList(provider));
            Mockito.when(provider.to()).then(invocation -> Object.class);
            Mockito.when(provider.from()).then(invocation -> Object.class);
            Mockito.when(provider.converter()).then(invocation -> c);

            ConverterService service = ConverterService.getInstance();
            Assertions.assertSame(service, ConverterService.getInstance());
        }
    }
}
