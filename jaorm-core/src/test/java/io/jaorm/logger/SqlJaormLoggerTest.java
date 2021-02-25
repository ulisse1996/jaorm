package io.jaorm.logger;

import io.jaorm.entity.sql.SqlParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.logging.Level;

@ExtendWith(MockitoExtension.class)
class SqlJaormLoggerTest {

    @Mock private List<SqlParameter> mockParameters;

    @Test
    void should_only_log_sql() {
        JaormLoggerConfiguration.setCurrent(
                JaormLoggerConfiguration.builder()
                    .setLevel(Level.SEVERE)
                    .build()
        );
        String mockSql = "MOCK";
        new SqlJaormLogger(SqlJaormLogger.class).logSql(mockSql, mockParameters);
        Mockito.verifyNoInteractions(mockParameters);
    }
}