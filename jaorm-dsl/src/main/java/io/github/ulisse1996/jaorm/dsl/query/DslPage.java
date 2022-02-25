package io.github.ulisse1996.jaorm.dsl.query;

import io.github.ulisse1996.jaorm.dsl.query.impl.SelectedImpl;
import io.github.ulisse1996.jaorm.entity.Page;

import java.util.List;
import java.util.Optional;

public class DslPage<T> extends Page<T> {

    private List<T> data;
    private final SelectedImpl<T, ?> selected;

    public DslPage(int pageNumber, int size, long count, SelectedImpl<T, ?> selected) {
        super(pageNumber, size, count);
        this.selected = selected;
    }

    @Override
    public Optional<Page<T>> getNext() {
        return hasNext()
                ? Optional.of(new DslPage<>(pageNumber + 1, fetchSize, count, selected))
                : Optional.empty();
    }

    @Override
    public Optional<Page<T>> getPrevious() {
        return hasPrevious()
                ? Optional.of(new DslPage<>(pageNumber + 1, fetchSize, count, selected))
                : Optional.empty();
    }

    @Override
    public List<T> getData() {
        if (this.data == null) {
            if (pageNumber != 0) {
                this.selected.setOffset(fetchSize * pageNumber);
            }
            this.selected.setLimit(fetchSize);
            this.data = this.selected.readAll();
        }

        return this.data;
    }
}
