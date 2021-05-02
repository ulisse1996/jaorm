package io.github.ulisse1996.transaction;

import io.github.ulisse1996.exception.JaormSqlException;
import io.github.ulisse1996.spi.TransactionManager;
import io.github.ulisse1996.transaction.exception.UnexpectedException;

import java.sql.SQLException;
import java.util.concurrent.Callable;

public class Transactional {

    private Transactional() {}

    private static void safeRollback() {
        try {
            TransactionManager.getInstance().getCurrentTransaction().rollback();
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <X extends Exception, V> V exec(Callable<V> callable, Class<X> exceptionClass) throws X {
        try {
            TransactionManager.getInstance().createTransaction();
            V obj = callable.call();
            TransactionManager.getInstance().getCurrentTransaction().commit();
            return obj;
        } catch (Exception ex) {
            safeRollback();
            if (exceptionClass.isInstance(ex)) {
                throw (X) ex;
            } else {
                throw new UnexpectedException(ex);
            }
        }
    }
}
