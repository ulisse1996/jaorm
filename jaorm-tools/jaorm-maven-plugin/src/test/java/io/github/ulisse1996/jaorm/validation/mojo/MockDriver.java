package io.github.ulisse1996.jaorm.validation.mojo;

import org.mockito.Mockito;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class MockDriver implements Driver {

    private static final Connection CONNECTION = Mockito.mock(Connection.class);

    public static void reInit() {
        Mockito.reset(CONNECTION);
    }

    public static Connection getConnection() {
        return CONNECTION;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return CONNECTION;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return true;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
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
