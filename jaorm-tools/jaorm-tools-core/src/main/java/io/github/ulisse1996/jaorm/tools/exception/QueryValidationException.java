package io.github.ulisse1996.jaorm.tools.exception;

import net.sf.jsqlparser.JSQLParserException;

public class QueryValidationException extends Exception {

    public QueryValidationException(Exception ex) {
        super(ex);
    }

    public QueryValidationException(String message) {
        super(message);
    }
}
