package io.github.ulisse1996.jaorm.mapping;

import io.github.ulisse1996.jaorm.ResultSetExecutor;
import io.github.ulisse1996.jaorm.SqlExecutor;
import io.github.ulisse1996.jaorm.SqlUtil;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class JaormCursor<T> implements Cursor<T> {

    private final Supplier<EntityDelegate<T>> supplier;
    private final boolean fetched;
    private final Connection connection;
    private final PreparedStatement preparedStatement;
    private final ResultSetExecutor executor;

    public JaormCursor(Connection connection, PreparedStatement preparedStatement,
                       SqlExecutor executor, Supplier<EntityDelegate<T>> supplier) {
        this.fetched = false;
        this.connection = connection;
        this.preparedStatement = preparedStatement;
        this.executor = (ResultSetExecutor) executor;
        this.supplier = supplier;
    }

    @Override
    public boolean isFetched() {
        return this.fetched;
    }

    @Override
    public void close() {
        SqlUtil.silentClose(this.executor, this.preparedStatement, this.connection);
    }

    @Override
    public Iterator<T> iterator() {
        if (!this.fetched) {
            return new CursorIterator<>(
                    this.executor.getResultSet(),
                    this.supplier,
                    this::close
            );
        } else {
            throw new IllegalStateException("Cursor is already open !");
        }
    }

    private static class CursorIterator<T> implements Iterator<T> {

        private final ResultSet resultSet;
        private final Supplier<EntityDelegate<T>> supplier;
        private final Runnable onClose;
        private boolean hasNext;

        CursorIterator(ResultSet rs, Supplier<EntityDelegate<T>> supplier,
                       Runnable onClose) {
            this.resultSet = rs;
            this.hasNext = canGoNext(rs);
            this.supplier = supplier;
            this.onClose = onClose;
        }

        @Override
        public boolean hasNext() {
            return this.hasNext;
        }

        @Override
        public T next() {
            if (!hasNext) {
                throw new NoSuchElementException("Cursor doesn't have next element !");
            }

            T next = mapNext();
            this.hasNext = hasNext();
            return next;
        }

        @SuppressWarnings("unchecked")
        private T mapNext() {
            try {
                EntityDelegate<T> entity = supplier.get();
                entity.setEntity(resultSet);
                return (T) entity;
            } catch (SQLException ex) {
                this.onClose.run();
                throw new JaormSqlException(ex);
            }
        }

        private boolean canGoNext(ResultSet rs) {
            try {
                return rs.next();
            } catch (SQLException ex) {
                this.onClose.run();
                throw new JaormSqlException(ex);
            }
        }
    }
}
