package io.github.ulisse1996.jaorm.vendor.specific;

public enum DriverType {
    ORACLE("Oracle"),
    POSTGRE("PostgreSQL"),
    MYSQL("MySQL"),
    DB2("AS/400"),
    MS_SQLSERVER("SQL Server");

    private final String matchName;

    DriverType(String matchName) {
        this.matchName = matchName;
    }

    public boolean match(String driverName) {
        return driverName.toUpperCase().contains(matchName.toUpperCase());
    }
}
