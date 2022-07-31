package io.github.ulisse1996.jaorm.mapping;

import io.github.ulisse1996.jaorm.SqlExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface JaormIterableResultProducer<T, R> {

    T produce(Connection connection, PreparedStatement preparedStatement,
              SqlExecutor executor, ThrowingFunction<ResultSet, R, SQLException> mapper);
}
