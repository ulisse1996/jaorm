package io.github.ulisse1996.exception;

import java.sql.SQLException;

public class JaormSqlException extends RuntimeException {

    public JaormSqlException(SQLException ex) {
        super(ex);
    }
}
