package io.github.ulisse1996.jaorm;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

class UpdateExecutorTest {

    @Test
    void should_do_nothing() throws SQLException {
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        try (UpdateExecutor ignored = new UpdateExecutor(preparedStatement, Collections.emptyList(), Collections.emptySet())) {
            Mockito.verify(preparedStatement, Mockito.times(1))
                    .executeUpdate();
        }
    }
}
