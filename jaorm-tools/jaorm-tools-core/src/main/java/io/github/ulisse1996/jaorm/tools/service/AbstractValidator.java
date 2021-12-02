package io.github.ulisse1996.jaorm.tools.service;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.processor.strategy.QueryStrategy;
import io.github.ulisse1996.jaorm.tools.cache.FileHashCache;
import io.github.ulisse1996.jaorm.tools.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.tools.exception.QueryValidationException;
import io.github.ulisse1996.jaorm.tools.logger.LogHolder;
import io.github.ulisse1996.jaorm.tools.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.tools.model.EntityMetadata;
import io.github.ulisse1996.jaorm.tools.model.TableMetadata;
import io.github.ulisse1996.jaorm.tools.service.sql.TableAliasPair;
import io.github.ulisse1996.jaorm.tools.service.sql.TableColumnFinder;
import io.github.ulisse1996.jaorm.tools.service.sql.TableColumnPair;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

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

    protected void validateQuery(String query, boolean noArgs) throws QueryValidationException {
        for (QueryStrategy strategy : QueryStrategy.values()) {
            if (strategy.isValid(query, noArgs)) {
                String realQuery = strategy.replaceQuery(query);
                try {
                    getLog().info(() -> String.format("Validating %s", realQuery));
                    Statement statement = CCJSqlParserUtil.parse(realQuery);
                    TableColumnFinder finder = new TableColumnFinder();
                    finder.getTableList(statement);
                    validateResult(finder);
                } catch (JSQLParserException | SQLException ex) {
                    throw new QueryValidationException(ex);
                }
                return;
            }
        }
        getLog().warn(() -> String.format("Can't validate %s for missing strategy !", query));
    }

    private void validateResult(TableColumnFinder finder) throws SQLException {
        List<TableAliasPair> tables = finder.getTables();
        List<TableColumnPair> columns = finder.getPairs();
        Map<String, List<TableColumnPair>> grouped = columns.stream()
                .collect(Collectors.groupingBy(TableColumnPair::getTable));
        for (Map.Entry<String, List<TableColumnPair>> entry : grouped.entrySet()) {
            String table = entry.getKey();
            if (validateAlias(tables, entry, table)) {
                continue;
            }
            if (table.isEmpty()) {
                doMultiFromValidation(tables, entry.getValue());
            }
        }
        List<TableColumnFinder> subQueries = finder.getSubQueries();
        for (TableColumnFinder subQuery : subQueries) {
            validateResult(subQuery);
        }
    }

    private boolean validateAlias(List<TableAliasPair> tables, Map.Entry<String, List<TableColumnPair>> entry, String table) throws SQLException {
        if (hasAlias(tables, table)) {
            table = getFromAlias(tables, table);
            executeSql(
                    String.format(
                            "SELECT %s FROM %s",
                            entry.getValue().stream().map(TableColumnPair::getColumn).collect(Collectors.joining(",")),
                            table
                    )
            );

            return true;
        }

        return false;
    }

    private void doMultiFromValidation(List<TableAliasPair> tables, List<TableColumnPair> columns) throws SQLException {
        List<TableAliasPair> withoutAlias = tables.stream()
                .filter(t -> !t.hasAlias())
                .collect(Collectors.toList());
        executeSql(
                String.format(
                        "SELECT %s FROM %s",
                        columns.stream().map(TableColumnPair::getColumn).distinct().collect(Collectors.joining(",")),
                        withoutAlias.stream().map(TableAliasPair::getName).collect(Collectors.joining(","))
                )
        );
    }

    private String getFromAlias(List<TableAliasPair> tables, String table) {
        return tables.stream()
                .filter(t -> t.hasAlias() && t.getAlias().equalsIgnoreCase(table))
                .findFirst()
                .map(TableAliasPair::getName)
                .orElseThrow(() -> new IllegalArgumentException("Can't find alias"));
    }

    private boolean hasAlias(List<TableAliasPair> tables, String table) {
        return tables.stream()
                .anyMatch(t -> t.hasAlias() && t.getAlias().equalsIgnoreCase(table));
    }

    @SuppressWarnings("EmptyTryBlock")
    private void executeSql(String sql) throws SQLException {
        getLog().info(() -> String.format("Checking definition %s", sql));
        try (Connection connection = getConnection();
            PreparedStatement pr = connection.prepareStatement(sql);
            ResultSet ignored = pr.executeQuery()) { //NOSONAR
        }
    }

    protected void validate(String klassName, EntityMetadata entityMetadata, String table) throws EntityValidationException, SQLException {
        getLog().info(() -> String.format("Checking Table %s", table));
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

        Properties properties = new Properties();
        properties.put("user", this.connectionInfo.getJdbcUsername());
        properties.put("password", this.connectionInfo.getJdbcPassword());
        return driver.connect(this.connectionInfo.getJdbcUrl(), properties);
    }
}
