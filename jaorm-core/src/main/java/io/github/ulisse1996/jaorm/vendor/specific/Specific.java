package io.github.ulisse1996.jaorm.vendor.specific;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public interface Specific {

    default boolean supportSpecific() {
        try (Connection connection = DataSourceProvider.getCurrent()
                .getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return getDriverType().match(metaData.getDriverName());
        } catch (SQLException ex) {
            return false;
        }
    }

    DriverType getDriverType();
}
