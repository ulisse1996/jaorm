package io.github.ulisse1996.jaorm.transaction;

import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionManagerImplTest {

    @BeforeEach
    public void removeTransaction() {
        TransactionManagerImpl.TRANSACTION_THREAD_LOCAL.remove();
    }

    @Test
    void should_return_null_transaction() {
        TransactionManagerImpl transactionManager = new TransactionManagerImpl();
        Assertions.assertNull(transactionManager.getCurrentTransaction());
    }

    @Test
    void should_set_current_transaction() {
        TransactionManagerImpl transactionManager = new TransactionManagerImpl();
        Assertions.assertNull(transactionManager.getCurrentTransaction());
        transactionManager.createTransaction();
        Assertions.assertNotNull(transactionManager.getCurrentTransaction());
    }

    @Test
    void should_create_delegate() {
        TransactionManagerImpl transactionManager = new TransactionManagerImpl();
        Assertions.assertDoesNotThrow(() -> transactionManager.createDelegate(Mockito.mock(DataSourceProvider.class))); //NOSONAR
    }
}
