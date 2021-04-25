package io.jaorm.custom;

import io.jaorm.ServiceFinder;
import io.jaorm.entity.sql.SqlAccessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CustomFeaturesTest {

    private static final SqlAccessor MOCK = new SqlAccessor(
            MyEnum.class,
                        (rs, colName) -> MyEnum.VAL,
            (pr, index, value) -> {}
    ) {};
    private static final SqlAccessorFeature SQL_ACCESSOR_FEATURE = new SqlAccessorFeature() {
        @Override
        public <R> SqlAccessor findCustom(Class<R> klass) {
            if (MyEnum.class.equals(klass)) {
                return MOCK;
            }

            return null;
        }
    };

    private enum MyEnum {
        VAL
    }

    @Test
    void should_return_default_value_using_feature() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(SqlAccessorFeature.class))
                    .thenReturn(SQL_ACCESSOR_FEATURE);
            SqlAccessor accessor = SqlAccessor.find(MyEnum.class);
            Assertions.assertSame(MOCK, accessor);
        }
    }

    @Test
    void should_throw_unsupported_exception_for_not_enabled_feature() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadService(SqlAccessorFeature.class))
                    .thenThrow(IllegalArgumentException.class);
            CustomFeatures.DefaultFeature<SqlAccessorFeature> feature =
                    new CustomFeatures.DefaultFeature<>(SqlAccessorFeature.class);
            Assertions.assertThrows(UnsupportedOperationException.class, feature::getFeature);
        }
    }
}
