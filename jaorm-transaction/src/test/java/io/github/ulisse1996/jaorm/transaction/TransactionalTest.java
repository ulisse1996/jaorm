package io.github.ulisse1996.jaorm.transaction;

import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.spi.TransactionManager;
import io.github.ulisse1996.jaorm.transaction.exception.UnexpectedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.SQLException;

class TransactionalTest {

    @Test
    void should_return_object_after_exec() throws SQLException {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        Transaction transaction = Mockito.mock(Transaction.class);
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            Object object = new Object();
            Object result = Transactional.exec(() -> object, IllegalArgumentException.class);
            Assertions.assertSame(object, result);
            Mockito.verify(manager, Mockito.times(1))
                    .getCurrentTransaction();
            Mockito.verify(transaction, Mockito.times(1))
                    .commit();
        }
    }

    @Test
    void should_throw_expected_exception() throws SQLException {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        Transaction transaction = Mockito.mock(Transaction.class);
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            try {
                Transactional.exec(() -> {
                    throw new IllegalArgumentException();
                }, IllegalArgumentException.class);
            } catch (Exception ex) {
                Assertions.assertTrue(ex instanceof IllegalArgumentException);
                Mockito.verify(manager, Mockito.times(1))
                        .getCurrentTransaction();
                Mockito.verify(transaction, Mockito.times(1))
                        .rollback();
            }
        }
    }

    @Test
    void should_throw_unexpected_exception() throws SQLException {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        Transaction transaction = Mockito.mock(Transaction.class);
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            try {
                Transactional.exec(() -> {
                    throw new IllegalStateException();
                }, IllegalArgumentException.class);
            } catch (Exception ex) {
                Assertions.assertTrue(ex instanceof UnexpectedException);
                Assertions.assertTrue(ex.getCause() instanceof IllegalStateException);
                Mockito.verify(manager, Mockito.times(1))
                        .getCurrentTransaction();
                Mockito.verify(transaction, Mockito.times(1))
                        .rollback();
            }
        }
    }

    @Test
    void should_throw_jaorm_sql_exception_for_rollback_error() throws SQLException {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        Transaction transaction = Mockito.mock(Transaction.class);
        try (MockedStatic<TransactionManager> mk = Mockito.mockStatic(TransactionManager.class)) {
            mk.when(TransactionManager::getInstance)
                    .thenReturn(manager);
            Mockito.when(manager.getCurrentTransaction())
                    .thenReturn(transaction);
            Mockito.doThrow(SQLException.class)
                    .when(transaction).rollback();
            try {
                Transactional.exec(() -> {
                    throw new IllegalStateException();
                }, IllegalArgumentException.class);
            } catch (Exception ex) {
                Assertions.assertTrue(ex instanceof JaormSqlException);
            }
        }
    }
}
