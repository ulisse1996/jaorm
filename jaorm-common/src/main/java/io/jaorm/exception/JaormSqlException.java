package io.jaorm.exception;

import java.sql.SQLException;

public class JaormSqlException extends RuntimeException {

    public JaormSqlException(SQLException ex) {
        super(ex);
    }
}
