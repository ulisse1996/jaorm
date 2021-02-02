package io.jaorm.integration.test;

import io.jaorm.entity.sql.DataSourceProvider;
import org.hsqldb.jdbc.JDBCDataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class HSQLDBProvider extends DataSourceProvider {

    private static final ThreadLocal<DataSource> LOCAL_DATASOURCE = ThreadLocal.withInitial(() -> null);

    public static void clear() {
        LOCAL_DATASOURCE.remove();
    }

    @Override
    public DataSource getDataSource() {
        if (LOCAL_DATASOURCE.get() == null) {
            LOCAL_DATASOURCE.set(createDatasource(DatabaseType.ORACLE));
        }

        return LOCAL_DATASOURCE.get();
    }

    public static void createFor(DatabaseType type) {
        LOCAL_DATASOURCE.set(createDatasource(type));
    }

    private static DataSource createDatasource(DatabaseType type) {
        try {
            Properties prop = new Properties();
            prop.put("url", "jdbc:hsqldb:mem:jaorm;" + type.getSyntax());
            prop.put("user", "jaorm");
            prop.put("password", "");
            return JDBCDataSourceFactory.createDataSource(prop);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public enum DatabaseType {
        ORACLE("sql.syntax_ora=true");

        private final String syntax;

        DatabaseType(String syntax) {
            this.syntax = syntax;
        }

        public String getSyntax() {
            return syntax;
        }
    }
}
