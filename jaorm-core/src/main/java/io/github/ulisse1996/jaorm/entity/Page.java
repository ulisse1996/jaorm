package io.github.ulisse1996.jaorm.entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class Page<T> {

    private static final Page<Object> EMPTY_PAGE = new EmptyPage();
    protected final int pageNumber;
    protected final int fetchSize;
    protected final long count;

    protected Page(int pageNumber, int fetchSize, long count) {
        this.pageNumber = assertGreaterEqualsZero(pageNumber);
        this.fetchSize = assertGreaterZero(fetchSize);
        this.count = count;
    }

    private int assertGreaterEqualsZero(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Page Number can't be less than 0!");
        }

        return n;
    }

    private int assertGreaterZero(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Size can't be 0!");
        }

        return n;
    }

    public boolean hasNext() {
        int offset = pageNumber == 0 ? fetchSize : fetchSize * pageNumber;
        return count > offset;
    }

    public boolean hasPrevious() {
        return pageNumber != 0;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public int getFetchSize() {
        return this.fetchSize;
    }

    public long getCount() {
        return this.count;
    }

    @SuppressWarnings("unchecked")
    public static <R> Page<R> empty() {
        return (Page<R>) EMPTY_PAGE;
    }

    public abstract Optional<Page<T>> getNext();
    public abstract Optional<Page<T>> getPrevious();
    public abstract List<T> getData();

    private static class EmptyPage extends Page<Object> {

        protected EmptyPage() {
            super(0, Integer.MAX_VALUE, 0);
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public Optional<Page<Object>> getNext() {
            return Optional.empty();
        }

        @Override
        public Optional<Page<Object>> getPrevious() {
            return Optional.empty();
        }

        @Override
        public List<Object> getData() {
            return Collections.emptyList();
        }
    }
}
