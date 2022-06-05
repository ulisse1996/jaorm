package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.Sort;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PageImplTest {

    @Mock private EntityDelegate<?> delegate;
    @Mock private DelegatesService delegatesService;
    @Mock private LimitOffsetSpecific specific;
    @Mock private QueryRunner runner;

    @Test
    void should_return_empty_next_page() {
        Page<Object> page = new PageImpl<>(
                0,
                10,
                10,
                Object.class,
                Collections.emptyList()
        );
        Assertions.assertFalse(page.getNext().isPresent());
    }

    @Test
    void should_return_empty_previous_page() {
        Page<Object> page = new PageImpl<>(
                0,
                10,
                10,
                Object.class,
                Collections.emptyList()
        );
        Assertions.assertFalse(page.getPrevious().isPresent());
    }

    @Test
    void should_return_next_page() {
        Page<Object> page = new PageImpl<>(
                0,
                10,
                20,
                Object.class,
                Collections.emptyList()
        );
        Optional<Page<Object>> next = page.getNext();
        Assertions.assertTrue(next.isPresent());
        Assertions.assertEquals(1, next.get().getPageNumber());
    }

    @Test
    void should_return_previous_page() {
        Page<Object> page = new PageImpl<>(
                1,
                10,
                20,
                Object.class,
                Collections.emptyList()
        );
        Optional<Page<Object>> next = page.getPrevious();
        Assertions.assertTrue(next.isPresent());
        Assertions.assertEquals(0, next.get().getPageNumber());
    }

    @Test
    void should_return_empty_page() {
        Page<Object> empty = Page.empty();
        Assertions.assertEquals(0, empty.getPageNumber());
        Assertions.assertEquals(Integer.MAX_VALUE, empty.getFetchSize());
        Assertions.assertEquals(0, empty.getCount());
        Assertions.assertFalse(empty.hasNext());
        Assertions.assertFalse(empty.hasPrevious());
        Assertions.assertFalse(empty.getNext().isPresent());
        Assertions.assertFalse(empty.getPrevious().isPresent());
        Assertions.assertEquals(Collections.emptyList(), empty.getData());
    }

    @Test
    void should_throw_exception_for_bad_page_number() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PageImpl<>(-1, 0, 0, Object.class, Collections.emptyList())); //NOSONAR
    }

    @Test
    void should_throw_exception_for_bad_fetch_size_number() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PageImpl<>(0, 0, 0, Object.class, Collections.emptyList())); //NOSONAR
    }

    @Test
    void should_return_selected_data() {
        List<Object> data = Collections.nCopies(10, new Object());
        Page<Object> page = new PageImpl<>(0, 10, 10, Object.class, Collections.emptyList());
        setData(page, data);
        Assertions.assertEquals(data, page.getData());
    }

    @Test
    void should_fetch_data() {
        List<Object> data = Collections.nCopies(10, new Object());
        Page<Object> page = new PageImpl<>(0, 10, 10, Object.class,
                Collections.singletonList(Sort.asc(SqlColumn.instance(Object.class, "COL", String.class))));
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkVendor.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(specific);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getBaseSql()).thenReturn("SQL");
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(data);

            Assertions.assertEquals(data, page.getData());
            Mockito.verify(specific, Mockito.never())
                    .convertOffSetLimitSupport(Mockito.anyInt(), Mockito.anyInt());
            Mockito.verify(specific)
                    .convertOffSetLimitSupport(Mockito.anyInt());
        }
    }

    @Test
    void should_fetch_next_page_data() {
        List<Object> data = Collections.nCopies(10, new Object());
        Page<Object> page = new PageImpl<>(0, 10, 15, Object.class,
                Arrays.asList(
                    Sort.asc(SqlColumn.instance(Object.class, "COL", String.class)),
                    Sort.desc(SqlColumn.instance(Object.class, "COL_2", String.class))
                )
        );
        page = page.getNext()
                .orElseThrow(() -> new IllegalArgumentException("Can't find page !"));
        try (MockedStatic<DelegatesService> mk = Mockito.mockStatic(DelegatesService.class);
             MockedStatic<VendorSpecific> mkVendor = Mockito.mockStatic(VendorSpecific.class);
             MockedStatic<QueryRunner> mkQuery = Mockito.mockStatic(QueryRunner.class)) {
            mk.when(DelegatesService::getInstance)
                    .thenReturn(delegatesService);
            mkVendor.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(specific);
            mkQuery.when(() -> QueryRunner.getInstance(Mockito.any()))
                    .thenReturn(runner);
            Mockito.when(delegatesService.searchDelegate(Mockito.any()))
                    .thenReturn(() -> delegate);
            Mockito.when(delegate.getBaseSql()).thenReturn("SQL");
            Mockito.when(runner.readAll(Mockito.any(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(data);

            Assertions.assertEquals(data, page.getData());
            Mockito.verify(specific, Mockito.never())
                    .convertOffSetLimitSupport(Mockito.anyInt());
            Mockito.verify(specific)
                    .convertOffSetLimitSupport(Mockito.anyInt(), Mockito.anyInt());
        }
    }

    private void setData(Page<Object> page, List<Object> data) {
        try {
            Field f = PageImpl.class.getDeclaredField("data");
            f.setAccessible(true);
            f.set(page, data);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }
}
