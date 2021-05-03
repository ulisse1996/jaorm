package io.github.ulisse1996.jaorm.transaction;

import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

class TransactionImplTest {

    @Test
    void should_start_new_transaction() throws SQLException {
        TransactionImpl transaction = new TransactionImpl();
        Connection connection = Mockito.mock(Connection.class);
        Assertions.assertEquals(Transaction.Status.NONE, transaction.getStatus());
        transaction.start(connection);
        Assertions.assertEquals(Transaction.Status.STARTED, transaction.getStatus());
        Mockito.verify(connection, Mockito.times(1))
                .setAutoCommit(false);
    }

    @Test
    void should_commit_transaction() throws SQLException {
        setProvider();
        TransactionImpl transaction = new TransactionImpl();
        Connection connection = Mockito.mock(Connection.class);
        Assertions.assertEquals(Transaction.Status.NONE, transaction.getStatus());
        transaction.start(connection);
        transaction.commit();
        Assertions.assertEquals(Transaction.Status.COMMIT, transaction.getStatus());
        Mockito.verify(connection, Mockito.times(1))
                .commit();
        Mockito.verify(connection, Mockito.times(1))
                .close();
    }

    @Test
    void should_rollback_transaction() throws SQLException {
        setProvider();
        TransactionImpl transaction = new TransactionImpl();
        Connection connection = Mockito.mock(Connection.class);
        Assertions.assertEquals(Transaction.Status.NONE, transaction.getStatus());
        transaction.start(connection);
        transaction.rollback();
        Assertions.assertEquals(Transaction.Status.ROLLBACK, transaction.getStatus());
        Mockito.verify(connection, Mockito.times(1))
                .rollback();
        Mockito.verify(connection, Mockito.times(1))
                .close();
    }

    private void setProvider() {
        DataSourceProvider.setDelegate(Mockito.mock(DataSourceProvider.class));
    }

    @Test
    void should_ignore_exception_on_rollback_transaction() throws SQLException {
        setProvider();
        TransactionImpl transaction = new TransactionImpl();
        Connection connection = Mockito.mock(Connection.class);
        Assertions.assertEquals(Transaction.Status.NONE, transaction.getStatus());
        Mockito.doThrow(SQLException.class)
                .when(connection).close();
        transaction.start(connection);
        transaction.rollback();
        Assertions.assertEquals(Transaction.Status.ROLLBACK, transaction.getStatus());
        Mockito.verify(connection, Mockito.times(1))
                .rollback();
    }
}
