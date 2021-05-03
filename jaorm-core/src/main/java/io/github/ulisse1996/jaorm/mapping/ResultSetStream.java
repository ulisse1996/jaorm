package io.github.ulisse1996.jaorm.mapping;

import io.github.ulisse1996.jaorm.ResultSetExecutor;
import io.github.ulisse1996.jaorm.SqlExecutor;
import io.github.ulisse1996.jaorm.SqlUtil;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ResultSetStream<R> {

    private final Connection connection;
    private final PreparedStatement preparedStatement;
    private final ResultSetExecutor executor;
    private final Stream<R> stream;
    private final ThrowingFunction<ResultSet, R, SQLException> mapperFunction;

    public ResultSetStream(Connection connection, PreparedStatement preparedStatement, SqlExecutor executor,
                           ThrowingFunction<ResultSet, R, SQLException> mapperFunction) {
        this.connection = connection;
        this.preparedStatement = preparedStatement;
        this.executor = (ResultSetExecutor) executor;
        this.mapperFunction = mapperFunction;
        this.stream = createStream();
    }

    public Stream<R> getStream() {
        return stream;
    }

    private Stream<R> createStream() {
        return StreamSupport.stream(createSpliterator(), false)
                .onClose(() -> SqlUtil.silentClose(executor, preparedStatement, connection));
    }

    private Spliterator<R> createSpliterator() {
        return new Spliterators.AbstractSpliterator<R>(Long.MAX_VALUE, Spliterator.ORDERED) {

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
                try {
                    if (executor.getResultSet().next()) {
                        action.accept(mapperFunction.apply(executor.getResultSet()));
                        return true;
                    }

                    SqlUtil.silentClose(executor, preparedStatement, connection);
                    return false;
                } catch (SQLException ex) {
                    SqlUtil.silentClose(executor, preparedStatement, connection);
                    throw new JaormSqlException(ex);
                }
            }
        };
    }
}
