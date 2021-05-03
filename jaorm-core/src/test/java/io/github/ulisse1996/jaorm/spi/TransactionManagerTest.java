package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;

class TransactionManagerTest {

    @BeforeEach
    public void setUp() {
        TransactionManager.INSTANCE.set(null);
    }

    @Test
    void should_return_standard_implementation() {
        TransactionManager manager = Mockito.mock(TransactionManager.class);
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(TransactionManager.class))
                    .thenReturn(Collections.singletonList(manager));
            Assertions.assertSame(manager, TransactionManager.getInstance());
        }
    }

    @Test
    void should_select_no_op_instance() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(TransactionManager.class))
                    .thenReturn(Collections.emptyList());
            Assertions.assertSame(TransactionManager.NoOpTransactionManager.INSTANCE, TransactionManager.getInstance());
        }
    }

    @Test
    void should_do_nothing_for_no_op_create_transaction() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(TransactionManager.class))
                    .thenReturn(Collections.emptyList());
            Assertions.assertDoesNotThrow(TransactionManager.NoOpTransactionManager.INSTANCE::createTransaction);
        }
    }

    @Test
    void should_return_no_op_transaction() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(TransactionManager.class))
                    .thenReturn(Collections.emptyList());
            Transaction noOpTransaction = getNoOpTransaction();
            Assertions.assertSame(noOpTransaction, TransactionManager.getInstance().getCurrentTransaction());
        }
    }

    @Test
    void should_throw_unsupported_exception_for_delegate_creation() {
        try (MockedStatic<ServiceFinder> mk = Mockito.mockStatic(ServiceFinder.class)) {
            mk.when(() -> ServiceFinder.loadServices(TransactionManager.class))
                    .thenReturn(Collections.emptyList());
            Assertions.assertThrows(UnsupportedOperationException.class,() -> TransactionManager.getInstance() //NOSONAR
                    .createDelegate(Mockito.mock(DataSourceProvider.class)));
        }
    }

    @Test
    void should_do_nothing_for_no_op_transaction() {
        Transaction noOpTransaction = getNoOpTransaction();
        Assertions.assertNotNull(noOpTransaction);
        Assertions.assertDoesNotThrow(noOpTransaction::begin);
        Assertions.assertDoesNotThrow(noOpTransaction::commit);
        Assertions.assertDoesNotThrow(noOpTransaction::rollback);
        Assertions.assertEquals(Transaction.Status.NONE, noOpTransaction.getStatus());
    }

    private Transaction getNoOpTransaction() {
        try {
            Field instance = Class.forName(TransactionManager.NoOpTransactionManager.class.getName() + "$NoOpTransaction")
                    .getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            return (Transaction) instance.get(null);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }

        return null;
    }
}
