package io.github.ulisse1996.jaorm.tools.model;

public class ConnectionInfo {

    private final String jdbcDriver;
    private final String jdbcUrl;
    private final String jdbcUsername;
    private final String jdbcPassword;

    public ConnectionInfo(String jdbcDriver, String jdbcUrl, String jdbcUsername, String jdbcPassword) {
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.jdbcUsername = jdbcUsername;
        this.jdbcPassword = jdbcPassword;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }
}
