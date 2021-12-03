package io.github.ulisse1996.jaorm.exception;

import java.sql.SQLException;

public class JaormSqlException extends RuntimeException {

    public JaormSqlException(SQLException ex) {
        super(ex);
    }

    public JaormSqlException(String message) {
        super(message);
    }
}
