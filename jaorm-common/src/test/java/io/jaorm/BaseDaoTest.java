package io.jaorm;

import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.exception.JaormPersistEventException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.NoInteractions;
import org.mockito.verification.VerificationMode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class BaseDaoTest {

    @Test
    void should_transform_arguments_in_parameters() {
        final MyDao dao = new MyDao();
        Object[] values = new Object[] { 1, "TEST", null};

        List<SqlParameter> sqlParameters = dao.argumentsAsParameters(values);
        Assertions.assertFalse(sqlParameters.isEmpty());
        Assertions.assertAll(
                () -> Assertions.assertEquals(values[0], sqlParameters.get(0).getVal()),
                () -> Assertions.assertEquals(values[1], sqlParameters.get(1).getVal()),
                () -> Assertions.assertEquals(values[2], sqlParameters.get(2).getVal())
        );
    }

    @ParameterizedTest
    @MethodSource("getNPEExecutables")
    void should_throw_npe_for_null_input(Executable executable) {
        Assertions.assertThrows(NullPointerException.class, executable);
    }

    @Test
    void should_call_implemented_read() {
        final MyDao dao = Mockito.spy(new MyDao());
        dao.read(new DelegatesMock.MyEntity());

        Mockito.verify(dao).read(Mockito.any(Arguments.class));
    }

    @Test
    void should_call_implemented_read_Opt() {
        final MyDao dao = Mockito.spy(new MyDao());
        dao.readOpt(new DelegatesMock.MyEntity());

        Mockito.verify(dao).readOpt(Mockito.any(Arguments.class));
    }

    @Test
    void should_call_implemented_update() {
        final MyDao dao = Mockito.spy(new MyDao());
        dao.update(new DelegatesMock.MyEntity());

        Mockito.verify(dao).update(Mockito.any(Arguments.class), Mockito.any(Arguments.class));
    }

    @Test
    void should_call_implemented_update_without_keys() {
        final MyDao dao = Mockito.spy(new MyDao());
        dao.update(Arguments.empty());

        Mockito.verify(dao).update(Mockito.any(Arguments.class), Mockito.any(Arguments.class));
    }

    @Test
    void should_call_implemented_delete() {
        final MyDao dao = Mockito.spy(new MyDao());
        dao.delete(new DelegatesMock.MyEntity());

        Mockito.verify(dao).delete(Mockito.any(Arguments.class));
    }

    @Test
    void should_call_implemented_update_for_list() {
        final MyDao dao = Mockito.spy(new MyDao());
        dao.update(Collections.singletonList(new DelegatesMock.MyEntity()));

        Mockito.verify(dao).update(Mockito.any(Arguments.class), Mockito.any(Arguments.class));
    }

    @Test
    void should_call_implemented_delete_for_list() {
        final MyDao dao = Mockito.spy(new MyDao());
        dao.delete(Collections.singletonList(new DelegatesMock.MyEntity()));

        Mockito.verify(dao).delete(Mockito.any(Arguments.class));
    }

    @Test
    void should_throw_persist_exception_for_pre_persist() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        Mockito.doThrow(IllegalArgumentException.class)
                .when(entity).prePersist(Mockito.any());
        Assertions.assertThrows(JaormPersistEventException.class, () -> dao.insert(entity));
    }

    @Test
    void should_throw_persist_exception_for_post_persist() {
        final DelegatesMock.MyEntity entity = Mockito.spy(new DelegatesMock.MyEntity());
        final MyDao dao = new MyDao();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getCurrent)
                    .thenReturn(delegates);
            Mockito.when(delegates.getInsertSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.insert(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocationOnMock -> invocationOnMock.getArgument(0));
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(entity).postPersist(Mockito.any());
            Assertions.assertThrows(JaormPersistEventException.class, () -> dao.insert(entity));
        }
    }

    @Test
    void should_do_correct_insert() {
        final MyDao dao = new MyDao();
        final DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getCurrent)
                    .thenReturn(delegates);
            Mockito.when(delegates.getInsertSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.insert(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocationOnMock -> invocationOnMock.getArgument(0));
            Assertions.assertEquals(entity, dao.insert(entity));
        }
    }

    @Test
    void should_do_correct_list_insert() {
        final MyDao dao = new MyDao();
        final DelegatesMock.MyEntity entity = new DelegatesMock.MyEntity();
        QueryRunner runner = Mockito.mock(QueryRunner.class);
        DelegatesService delegates = Mockito.mock(DelegatesService.class);
        try (MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class);
             MockedStatic<DelegatesService> mkDelegates = Mockito.mockStatic(DelegatesService.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            mkDelegates.when(DelegatesService::getCurrent)
                    .thenReturn(delegates);
            Mockito.when(delegates.getInsertSql(Mockito.any()))
                    .thenReturn("TEST");
            Mockito.when(delegates.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(runner.insert(Mockito.any(), Mockito.any(), Mockito.any()))
                    .then(invocationOnMock -> invocationOnMock.getArgument(0));
            Assertions.assertEquals(Collections.singletonList(entity), dao.insert(Collections.singletonList(entity)));
        }
    }

    public static Stream<org.junit.jupiter.params.provider.Arguments> getNPEExecutables() {
        final MyDao thisDao = new MyDao();
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.read((DelegatesMock.MyEntity) null)),
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.readOpt((DelegatesMock.MyEntity) null)),
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.update((DelegatesMock.MyEntity) null)),
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.insert((DelegatesMock.MyEntity) null)),
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.delete((DelegatesMock.MyEntity) null)),
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.update((List<DelegatesMock.MyEntity>) null)),
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.delete((List<DelegatesMock.MyEntity>) null)),
                org.junit.jupiter.params.provider.Arguments.of((Executable)() -> thisDao.insert((List<DelegatesMock.MyEntity>) null))
        );
    }

    private static class MyDao implements BaseDao<DelegatesMock.MyEntity> {

        @Override
        public DelegatesMock.MyEntity read(Arguments wheres) {
            return null;
        }

        @Override
        public Optional<DelegatesMock.MyEntity> readOpt(Arguments wheres) {
            return Optional.empty();
        }

        @Override
        public List<DelegatesMock.MyEntity> readAll(Arguments where) {
            return null;
        }

        @Override
        public void update(Arguments arguments, Arguments where) {

        }

        @Override
        public void delete(Arguments where) {

        }
    }
}