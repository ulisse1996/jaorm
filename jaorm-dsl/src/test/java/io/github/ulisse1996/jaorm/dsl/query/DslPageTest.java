package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.dsl.query.impl.SelectedImpl;
import io.github.ulisse1996.jaorm.entity.Page;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DslPageTest {

    @Mock private SelectedImpl<Object, ?> selected;
    @Mock private LimitOffsetSpecific specific;

    @Test
    void should_return_next_page() {
        Page<Object> page = new DslPage<>(0, 10, 15, selected);
        Optional<Page<Object>> next = page.getNext();
        Assertions.assertTrue(next.isPresent());
    }

    @Test
    void should_return_previous_page() {
        Page<Object> page = new DslPage<>(1, 10, 15, selected);
        Optional<Page<Object>> previous = page.getPrevious();
        Assertions.assertTrue(previous.isPresent());
    }

    @Test
    void should_return_empty_next_page() {
        Page<Object> page = new DslPage<>(0, 10, 10, selected);
        Optional<Page<Object>> next = page.getNext();
        Assertions.assertFalse(next.isPresent());
    }

    @Test
    void should_return_empty_previous_page() {
        Page<Object> page = new DslPage<>(0, 10, 15, selected);
        Optional<Page<Object>> previous = page.getPrevious();
        Assertions.assertFalse(previous.isPresent());
    }

    @Test
    void should_return_saved_data() {
        List<Object> data = Collections.nCopies(10, new Object());
        Page<Object> page = new DslPage<>(0, 10, 15, selected);
        setData(page, data);
        Assertions.assertEquals(data, page.getData());
    }

    @Test
    void should_select_data_for_first_page() {
        Page<Object> page = new DslPage<>(0, 10, 15, selected);
        List<Object> data = Collections.nCopies(10, new Object());
        Mockito.when(selected.readAll())
                .thenReturn(data);

        try (MockedStatic<VendorSpecific> mk = Mockito.mockStatic(VendorSpecific.class)) {
            mk.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(new LimitOffset());
            Assertions.assertEquals(data, page.getData());
            Mockito.verify(selected, Mockito.never())
                    .setOffset(Mockito.anyInt());
            Mockito.verify(selected)
                    .setLimit(Mockito.anyInt());
        }
    }

    @Test
    void should_select_data_for_next_page() {
        Page<Object> page = new DslPage<>(1, 10, 15, selected);
        List<Object> data = Collections.nCopies(10, new Object());
        Mockito.when(selected.readAll())
                .thenReturn(data);

        try (MockedStatic<VendorSpecific> mk = Mockito.mockStatic(VendorSpecific.class)) {
            mk.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(new LimitOffset());
            Assertions.assertEquals(data, page.getData());
            Mockito.verify(selected)
                    .setOffset(Mockito.anyInt());
            Mockito.verify(selected)
                    .setLimit(Mockito.anyInt());
        }
    }

    @Test
    void should_throw_exception_for_missing_order_with_required_option() {
        Page<Object> page = new DslPage<>(1, 10, 15, selected);
        try (MockedStatic<VendorSpecific> mk = Mockito.mockStatic(VendorSpecific.class)) {
            mk.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(specific);
            Mockito.when(selected.hasOrders()).thenReturn(false);
            Mockito.when(specific.requiredOrder()).thenReturn(true);
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    page::getData
            );
        }
    }

    private void setData(Page<Object> page, List<Object> data) {
        try {
            Field f = DslPage.class.getDeclaredField("data");
            f.setAccessible(true);
            f.set(page, data);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    private static class LimitOffset implements LimitOffsetSpecific {

        @Override
        public String convertOffSetLimitSupport(int limitRow) {
            return String.format(" LIMIT %d", limitRow);
        }

        @Override
        public String convertOffsetSupport(int offset) {
            return String.format(" OFFSET %d ", offset);
        }

        @Override
        public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
            return String.format(" LIMIT %d OFFSET %d", limitRow, offsetRow);
        }
    }
}
