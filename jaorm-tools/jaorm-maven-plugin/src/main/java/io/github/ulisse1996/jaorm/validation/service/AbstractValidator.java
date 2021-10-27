package io.github.ulisse1996.jaorm.validation.service;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.validation.cache.FileHashCache;
import io.github.ulisse1996.jaorm.validation.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.validation.logger.LogHolder;
import io.github.ulisse1996.jaorm.validation.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.validation.model.EntityMetadata;
import io.github.ulisse1996.jaorm.validation.model.TableMetadata;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.Optional;

public abstract class AbstractValidator implements Validator {

    protected final FileHashCache cache;
    private final ConnectionInfo connectionInfo;
    private Driver driver;

    protected AbstractValidator(String projectRoot, ConnectionInfo connectionInfo) throws IOException {
        this.cache = FileHashCache.getInstance(projectRoot);
        this.connectionInfo = connectionInfo;
    }

    protected JaormLogger getLog() {
        return LogHolder.get();
    }

    protected void validate(String klassName, EntityMetadata entityMetadata, String table) throws EntityValidationException, SQLException {
        try (Connection connection = getConnection();
             PreparedStatement pr = connection.prepareStatement(String.format("SELECT * FROM %s WHERE 1 = 0", table));
             ResultSet rs = pr.executeQuery()) {
            TableMetadata metadata = new TableMetadata(rs.getMetaData());
            for (EntityMetadata.FieldMetadata fieldMetadata : entityMetadata.getFields()) {
                getLog().info(() -> String.format("Checking Field/Column %s of Entity %s", fieldMetadata.getName(), klassName));
                Optional<TableMetadata.ColumnMetadata> columnOpt = metadata.findColumn(fieldMetadata.getColumnName());
                if (!columnOpt.isPresent()) {
                    throw new EntityValidationException(
                            String.format("Column %s not found for Entity %s", fieldMetadata.getColumnName(), klassName)
                    );
                }
                TableMetadata.ColumnMetadata column = columnOpt.get();
                if (!column.matchType(
                        fieldMetadata.getConverterType() != null ? fieldMetadata.getConverterType() : fieldMetadata.getType()
                )) {
                    throw new EntityValidationException(
                            String.format("Field/Column %s in Entity %s mismatch type! Found [%s], required one of %s",
                                    fieldMetadata.getName(), klassName, fieldMetadata.getType(), column.getSupportedTypesName())
                    );
                }
            }
        }
    }

    private Connection getConnection() throws SQLException {
        if (this.driver == null) {
            try {
                this.driver = (Driver) Class.forName(this.connectionInfo.getJdbcDriver()).getConstructor().newInstance();
                DriverManager.registerDriver(this.driver);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | NoSuchMethodException | InvocationTargetException e) {
                throw new SQLException(e);
            }
        }

        return DriverManager.getConnection(this.connectionInfo.getJdbcUrl(),
                this.connectionInfo.getJdbcUsername(), this.connectionInfo.getJdbcPassword());
    }
}
