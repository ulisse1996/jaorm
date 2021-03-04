package io.jaorm.transaction;

import io.jaorm.exception.JaormSqlException;
import io.jaorm.spi.TransactionManager;
import io.jaorm.transaction.exception.UnexpectedException;

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
    public static <X extends Exception, V> V exec(Callable<V> runnable, Class<X> exceptionClass) throws X {
        try {
            TransactionManager.getInstance().createTransaction();
            V obj = runnable.call();
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
