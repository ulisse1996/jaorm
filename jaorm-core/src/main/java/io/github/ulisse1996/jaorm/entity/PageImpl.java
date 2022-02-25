package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.Sort;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PageImpl<T> extends Page<T> {

    private final Class<T> entityClass;
    private final List<Sort<T>> sorts;
    private List<T> data;

    public PageImpl(int pageNumber, int fetchSize, long count,
                    Class<T> entityClass, List<Sort<T>> sorts) {
        super(pageNumber, fetchSize, count);
        this.entityClass = entityClass;
        this.sorts = sorts;
    }

    @Override
    public Optional<Page<T>> getNext() {
        return hasNext()
                ? Optional.of(new PageImpl<>(getPageNumber() + 1, getFetchSize(), getCount(), entityClass, this.sorts))
                : Optional.empty();
    }

    @Override
    public Optional<Page<T>> getPrevious() {
        return hasPrevious()
                ? Optional.of(new PageImpl<>(getPageNumber() - 1, getFetchSize(), getCount(), entityClass, this.sorts))
                : Optional.empty();
    }

    @Override
    public List<T> getData() {
        if (this.data == null) {
            EntityDelegate<?> delegate = DelegatesService.getInstance().searchDelegate(this.entityClass).get();
            LimitOffsetSpecific specific = VendorSpecific.getSpecific(LimitOffsetSpecific.class);
            String sql = delegate.getBaseSql().concat(" ORDER BY ")
                    .concat(sorts.stream()
                            .map(s -> String.format(" %s %s", s.getColumn().getName(), s.getSortType()))
                            .collect(Collectors.joining(",")));
            if (pageNumber != 0) {
                int offset = fetchSize * pageNumber;
                sql += specific.convertOffSetLimitSupport(this.fetchSize, offset);
            } else {
                sql += specific.convertOffSetLimitSupport(this.fetchSize);
            }
            this.data = QueryRunner.getInstance(this.entityClass)
                    .readAll(this.entityClass, sql, Collections.emptyList());
        }

        return this.data;
    }
}
