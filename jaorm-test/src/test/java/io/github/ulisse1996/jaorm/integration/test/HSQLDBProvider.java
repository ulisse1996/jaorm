package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.entity.schema.TableInfo;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import org.hsqldb.jdbc.JDBCDataSourceFactory;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HSQLDBProvider extends DataSourceProvider {

    private DataSource dataSource;
    private static List<String> executedSql;

    public void set(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static List<String> getExecutedSql() {
        return executedSql;
    }

    @Override
    public DataSource getDataSource() {
        if (this.dataSource == null) {
            createFor(DatabaseType.ORACLE);
        }

        return this.dataSource;
    }

    @Override
    public DataSource getDataSource(TableInfo tableInfo) {
        if (this.dataSource == null) {
            createFor(DatabaseType.ORACLE);
        }

        return this.dataSource;
    }

    public void createFor(DatabaseType type) {
        this.dataSource = createDatasource(type);
        executedSql = new ArrayList<>();
    }

    private static DataSource createDatasource(DatabaseType type) {
        try {
            Properties prop = new Properties();
            prop.put("url", "jdbc:hsqldb:mem:jaorm;" + type.getSyntax());
            prop.put("user", "jaorm");
            prop.put("password", "");
            DataSource dataSource = JDBCDataSourceFactory.createDataSource(prop);
            return datasourceProxy(dataSource);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static DataSource datasourceProxy(DataSource dataSource) {
        return (DataSource) Proxy.newProxyInstance(HSQLDBProvider.class.getClassLoader(), new Class[] {DataSource.class}, (proxy, method, args) -> {
            if (method.getName().equalsIgnoreCase("getConnection")) {
                return connectionProxy(dataSource.getConnection());
            }

            return method.invoke(dataSource, args);
        });
    }

    private static Connection connectionProxy(Connection connection) {
        return (Connection) Proxy.newProxyInstance(HSQLDBProvider.class.getClassLoader(), new Class[] {Connection.class}, (proxy, method, args) -> {
            if (method.getName().equalsIgnoreCase("prepareStatement")) {
                executedSql.add((String) args[0]);
                return method.invoke(connection, args);
            }

            return method.invoke(connection, args);
        });
    }

    public enum DatabaseType {
        ORACLE("sql.syntax_ora=true"),
        POSTGRE("sql.syntax_pgs=true"),
        MYSQL("sql.syntax_mys=true"),
        DB2("sql.syntax_db2=true"),
        MS_SQLSERVER("sql.syntax_mss=true");

        private final String syntax;

        DatabaseType(String syntax) {
            this.syntax = syntax;
        }

        public String getSyntax() {
            return syntax;
        }
    }
}
