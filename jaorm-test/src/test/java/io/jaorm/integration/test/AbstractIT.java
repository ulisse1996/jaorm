package io.jaorm.integration.test;

import io.jaorm.ServiceFinder;
import io.jaorm.entity.sql.DataSourceProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractIT {

    private static MockedStatic<ServiceFinder> mk;

    @BeforeAll
    public static void setup() {
        HSQLDBProvider.clear();
        // Mock for JDK11
        mk = Mockito.mockStatic(ServiceFinder.class);
        mk.when(ServiceFinder.loadService(Mockito.any()))
                .then(invocation -> {
                    try {
                        return invocation.callRealMethod();
                    } catch (Exception ex) {
                        if (invocation.getArgument(0).equals(DataSourceProvider.class)) {
                            return new HSQLDBProvider();
                        }

                        return null;
                    }
                });
    }

    @AfterAll
    public static void closeMock() {
        mk.close();
    }

    protected void setDataSource(HSQLDBProvider.DatabaseType type, String initSql) {
        HSQLDBProvider.createFor(type);
        List<String> strings = readFile(initSql);
        strings.add(0, "DROP SCHEMA PUBLIC CASCADE");
        prepareDb(strings);
    }

    protected void prepareDb(List<String> statements) {
        for (String s : statements) {
            try (Connection cn = DataSourceProvider.getCurrent().getConnection();
                 PreparedStatement pr = cn.prepareStatement(s)){
                pr.execute();
            } catch (SQLException ex) {
                Assertions.fail(ex);
            }
        }
    }

    protected List<String> readFile(String initSql) {
        try {
            return Files.readAllLines(Paths.get(JaormDSLIT.class.getResource("/inits/" + initSql).toURI()));
        } catch (Exception ex) {
            Assertions.fail(ex);
            return Collections.emptyList();
        }
    }

    public static Stream<Arguments> getSqlTests() {
        return Stream.of(
                Arguments.arguments(HSQLDBProvider.DatabaseType.ORACLE, "oracle_init.sql")
        );
    }
}
