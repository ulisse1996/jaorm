package io.github.ulisse1996.jaorm;

import java.sql.SQLException;

public interface Transaction {

    void begin() throws SQLException;
    void commit() throws SQLException;
    void rollback() throws SQLException;
    Status getStatus();

    enum Status {
        COMMIT,
        ROLLBACK,
        STARTED,
        NONE
    }
}
