package io.github.ulisse1996.jaorm.mapping;

import io.github.ulisse1996.jaorm.ResultSetExecutor;
import io.github.ulisse1996.jaorm.SqlExecutor;
import io.github.ulisse1996.jaorm.SqlUtil;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class JaormCursor<T> implements Cursor<T> {

    private final ThrowingFunction<ResultSet, T, SQLException> mapper;
    private boolean fetched;
    private final Connection connection;
    private final PreparedStatement preparedStatement;
    private final ResultSetExecutor executor;
    private boolean closed;

    public JaormCursor(Connection connection, PreparedStatement preparedStatement,
                       SqlExecutor executor, ThrowingFunction<ResultSet, T, SQLException> mapper) {
        this.fetched = false;
        this.connection = connection;
        this.preparedStatement = preparedStatement;
        this.executor = (ResultSetExecutor) executor;
        this.mapper = mapper;
    }

    @Override
    public boolean isFetched() {
        return this.fetched;
    }

    @Override
    public void close() {
        SqlUtil.silentClose(this.executor, this.preparedStatement, this.connection);
        this.closed = true;
    }

    @Override
    public Iterator<T> iterator() {
        if (!this.closed && !this.fetched) {
            this.fetched = true;
            return new CursorIterator<>(
                    this.executor.getResultSet(),
                    this.mapper,
                    this::close
            );
        } else {
            throw new IllegalStateException("Cursor is already consumed !");
        }
    }

    private static class CursorIterator<T> implements Iterator<T> {

        private final ResultSet resultSet;
        private final ThrowingFunction<ResultSet, T, SQLException> mapper;
        private final Runnable onClose;
        private boolean hasNext;

        CursorIterator(ResultSet rs, ThrowingFunction<ResultSet, T, SQLException> mapper,
                       Runnable onClose) {
            this.onClose = onClose;
            this.resultSet = rs;
            this.hasNext = canGoNext(rs);
            this.mapper = mapper;
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
            this.hasNext = canGoNext(resultSet);
            return next;
        }

        private T mapNext() {
            try {
                return mapper.apply(this.resultSet);
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
