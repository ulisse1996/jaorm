package io.jaorm.dsl;

import io.jaorm.QueryRunner;
import io.jaorm.dsl.impl.LikeType;
import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.EntityDelegate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class JaormTest {

    @Test
    void should_throw_unsupported_for_instantiation() {
        try {
            Constructor<Jaorm> declaredConstructor = Jaorm.class.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            declaredConstructor.newInstance();
        } catch (Exception ex) {
            Assertions.assertTrue(ex.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Test
    void should_throw_exception_for_missing_where_column_in_entity() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            mk.when(DelegatesService::getCurrent)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});
            Assertions.assertThrows(IllegalArgumentException.class, () -> Jaorm.select(Object.class).where("NOT_VALID")); //NOSONAR
        }
    }

    @Test
    void should_throw_exception_for_orWhere_before_and() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            mk.when(DelegatesService::getCurrent)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});
            Assertions.assertThrows(UnsupportedOperationException.class, () -> Jaorm.select(Object.class).orWhere("NOT_VALID")); //NOSONAR
        }
    }

    @Test
    void should_do_simple_read() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getCurrent)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});

            Mockito.when(runner.read(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(expected);

            Object result = Jaorm.select(Object.class)
                    .where("COL1").eq(2)
                    .where("COL1").ne(3)
                    .where("COL1").lt(4)
                    .where("COL1").gt(5)
                    .where("COL1").le(6)
                    .where("COL1").ge(7)
                    .where("COL1").isNull()
                    .where("COL1").isNotNull()
                    .where("COL1").in(Arrays.asList(1, 2, 3))
                    .where("COL1").notIn(Arrays.asList(1,2,3))
                    .where("COL1").like(LikeType.FULL,"TEST")
                    .read();
            Assertions.assertSame(expected, result);
        }
    }

    @Test
    void should_do_simple_read_opt() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getCurrent)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});

            Mockito.when(runner.readOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(expected));

            Optional<Object> result = Jaorm.select(Object.class)
                    .where("COL1").eq(2)
                    .orWhere("COL1").eq(4)
                    .readOpt();
            Assertions.assertTrue(result.isPresent());
            Assertions.assertSame(expected, result.get());
        }
    }

    @Test
    void should_do_simple_read_all() {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> run = Mockito.mockStatic(QueryRunner.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            QueryRunner runner = Mockito.mock(QueryRunner.class);
            Object expected = new Object();
            mk.when(DelegatesService::getCurrent)
                    .thenReturn(delegatesService);
            run.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL1"});

            Mockito.when(runner.readAll(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Collections.singletonList(expected));

            List<Object> result = Jaorm.select(Object.class)
                    .where("COL1").eq(2).or("COL1").eq(3)
                    .orWhere("COL1").eq(4).and("COL1").ne(3)
                    .readAll();
            Assertions.assertEquals(1, result.size());
            Assertions.assertSame(expected, result.get(0));
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("getNPEExecutables")
    void should_throw_NPE_for_missing_parameters(String name, Executable executable) {
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class)) {
            DelegatesService delegatesService = Mockito.mock(DelegatesService.class);
            EntityDelegate<?> delegate = Mockito.mock(EntityDelegate.class);
            mk.when(DelegatesService::getCurrent)
                    .thenReturn(delegatesService);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable())
                    .thenReturn("TABLE");
            Mockito.when(delegate.getSelectables())
                    .thenReturn(new String[] {"COL"});
            Assertions.assertThrows(NullPointerException.class, executable, "Error during NPE check for " + name);
        }
    }

    public static Stream<Arguments> getNPEExecutables() {
        return Stream.of(
                Arguments.of("Entity Class is null", (Executable)() -> Jaorm.select(null)),
                Arguments.of("Column where is null", (Executable)() -> Jaorm.select(Object.class).where(null)),
                Arguments.of("Eq value is null", (Executable)() -> Jaorm.select(Object.class).where("COL").eq(null)),
                Arguments.of("Ne value is null", (Executable)() -> Jaorm.select(Object.class).where("COL").ne(null)),
                Arguments.of("Lt value is null", (Executable)() -> Jaorm.select(Object.class).where("COL").lt(null)),
                Arguments.of("Gt values is null", (Executable)() -> Jaorm.select(Object.class).where("COL").gt(null)),
                Arguments.of("Le value is null", (Executable)() -> Jaorm.select(Object.class).where("COL").le(null)),
                Arguments.of("Ge value is null", (Executable)() -> Jaorm.select(Object.class).where("COL").ge(null)),
                Arguments.of("In with Iterable is null", (Executable)() -> Jaorm.select(Object.class).where("COL").in(null)),
                Arguments.of("Not In with SubQuery is null", (Executable)() -> Jaorm.select(Object.class).where("COL").notIn(null)),
                Arguments.of("Like with null type", (Executable)() -> Jaorm.select(Object.class).where("COL").like(null, "")),
                Arguments.of("Like value is null", (Executable)() -> Jaorm.select(Object.class).where("COL").like(LikeType.FULL, null))
        );
    }
}