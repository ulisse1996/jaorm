package io.github.ulisse1996.jaorm.vendor.specific;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class MergeSpecificTest {

    private static final SqlColumn<Object, String> USING_COL = SqlColumn.instance(Object.class, "COL1", String.class);
    private final MergeSpecific specific = new MockMerge(true);
    private final MergeSpecific notStandardSpecific = new MockMerge(false);
    @Mock private DelegatesService delegates;
    @Mock private EntityDelegate<?> delegate;
    @Mock private AliasesSpecific aliasesSpecific;
    @Mock private QueryRunner runner;
    @Mock private EntityMapper<?> mapper;
    @Mock private EntityMapper.ColumnMapper<?> columnMapper1;
    @Mock private EntityMapper.ColumnMapper<?> columnMapper2;

    @Test
    void should_create_merge_with_only_insert() {
        String expected = "MERGE INTO TABLE M USING ( SELECT ? COL1 ) H ON ( M.COL1 = H.COL1 ) WHEN NOT MATCHED THEN INSERT INTO MY_TABLE (COL1, COL2) VALUES (?,?)";
        try (MockedStatic<VendorSpecific> mk = Mockito.mockStatic(VendorSpecific.class);
             MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any())).thenReturn(runner);
            mkDel.when(DelegatesService::getInstance).thenReturn(delegates);
            mk.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                    .thenReturn(aliasesSpecific);
            Mockito.when(delegates.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable()).thenReturn("TABLE");
            Mockito.when(delegate.getInsertSql()).thenReturn("INSERT INTO MY_TABLE (COL1, COL2) VALUES (?,?)");
            Mockito.when(delegates.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(aliasesSpecific.convertToAlias(Mockito.any()))
                    .then(i -> " " + i.getArgument(0));

            specific.executeMerge(
                    Object.class,
                    Collections.singletonMap(USING_COL, ""),
                    Collections.singletonList(USING_COL),
                    null,
                    new Object()
            );

            Mockito.verify(runner)
                    .update(Mockito.eq(expected), Mockito.any());
            Mockito.verify(delegate, Mockito.never())
                    .getEntityMapper();
        }
    }

    @Test
    void should_create_merge_with_only_update() {
        String expected = "MERGE INTO TABLE M USING ( SELECT ? COL1 ) H ON ( M.COL1 = H.COL1 ) WHEN MATCHED THEN UPDATE SET M.COL2 = ?, M.COL3 = ?";
        try (MockedStatic<VendorSpecific> mk = Mockito.mockStatic(VendorSpecific.class);
             MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any())).thenReturn(runner);
            mkDel.when(DelegatesService::getInstance).thenReturn(delegates);
            mk.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                    .thenReturn(aliasesSpecific);
            Mockito.when(delegates.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable()).thenReturn("TABLE");
            Mockito.when(aliasesSpecific.convertToAlias(Mockito.any()))
                    .then(i -> " " + i.getArgument(0));
            Mockito.when(delegate.getEntityMapper())
                    .then(i -> mapper);
            Mockito.when(mapper.getMappers())
                    .then(i -> Arrays.asList(columnMapper1, columnMapper2));
            Mockito.when(mapper.getValues(Mockito.any(), Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(columnMapper1.getName())
                    .thenReturn("COL2");
            Mockito.when(columnMapper2.getName())
                    .thenReturn("COL3");

            specific.executeMerge(
                    Object.class,
                    Collections.singletonMap(USING_COL, ""),
                    Collections.singletonList(USING_COL),
                    new Object(),
                    null
            );

            Mockito.verify(runner)
                    .update(Mockito.eq(expected), Mockito.any());
            Mockito.verify(delegates, Mockito.never())
                    .asInsert(Mockito.any());
        }
    }

    @Test
    void should_create_merge_with_update_and_insert() {
        String expected = "MERGE INTO TABLE M USING ( SELECT ? COL1 ) H ON ( M.COL1 = H.COL1 ) WHEN NOT MATCHED THEN INSERT INTO MY_TABLE (COL1, COL2) VALUES (?,?) WHEN MATCHED THEN UPDATE SET M.COL2 = ?, M.COL3 = ?";
        try (MockedStatic<VendorSpecific> mk = Mockito.mockStatic(VendorSpecific.class);
             MockedStatic<DelegatesService> mkDel = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<QueryRunner> mkRunner = Mockito.mockStatic(QueryRunner.class)) {
            mkRunner.when(() -> QueryRunner.getInstance(Mockito.any())).thenReturn(runner);
            mkDel.when(DelegatesService::getInstance).thenReturn(delegates);
            mk.when(() -> VendorSpecific.getSpecific(AliasesSpecific.class))
                    .thenReturn(aliasesSpecific);
            Mockito.when(delegates.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getTable()).thenReturn("TABLE");
            Mockito.when(aliasesSpecific.convertToAlias(Mockito.any()))
                    .then(i -> " " + i.getArgument(0));

            // Update
            Mockito.when(delegate.getEntityMapper())
                    .then(i -> mapper);
            Mockito.when(mapper.getMappers())
                    .then(i -> Arrays.asList(columnMapper1, columnMapper2));
            Mockito.when(mapper.getValues(Mockito.any(), Mockito.any()))
                    .thenReturn(Arguments.empty());
            Mockito.when(columnMapper1.getName())
                    .thenReturn("COL2");
            Mockito.when(columnMapper2.getName())
                    .thenReturn("COL3");

            // Insert
            Mockito.when(delegate.getInsertSql()).thenReturn("INSERT INTO MY_TABLE (COL1, COL2) VALUES (?,?)");
            Mockito.when(delegates.asInsert(Mockito.any()))
                    .thenReturn(Arguments.empty());

            specific.executeMerge(
                    Object.class,
                    Collections.singletonMap(USING_COL, ""),
                    Collections.singletonList(USING_COL),
                    new Object(),
                    new Object()
            );

            Mockito.verify(runner)
                    .update(Mockito.eq(expected), Mockito.any());
        }
    }

    @Test
    void should_call_alternative_merge() {
        Assertions.assertDoesNotThrow(() ->
                notStandardSpecific.executeMerge(
                        Object.class,
                        Collections.emptyMap(),
                        Collections.emptyList(),
                        new Object(),
                        new Object()
                ));
    }

    private static class MockMerge extends MergeSpecific {

        private final boolean standard;

        private MockMerge(boolean standard) {
            this.standard = standard;
        }

        @Override
        public String fromUsing() {
            return "";
        }

        @Override
        public String appendAdditionalSql() {
            return "";
        }

        @Override
        public boolean isStandardMerge() {
            return standard;
        }

        @Override
        public <T> void executeAlternativeMerge(Class<T> klass, Map<SqlColumn<T, ?>, ?> usingColumns, List<SqlColumn<T, ?>> onColumns, T updateEntity, T insertEntity) {

        }
    }
}
