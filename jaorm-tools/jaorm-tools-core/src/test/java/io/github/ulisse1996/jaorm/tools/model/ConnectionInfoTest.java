package io.github.ulisse1996.jaorm.tools.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConnectionInfoTest {

    private final ConnectionInfo connectionInfo = new ConnectionInfo("DRIVER", "URL", "USERNAME", "PASSWORD");

    @Test
    void should_check_model() {
        Assertions.assertAll(
                () -> Assertions.assertEquals("DRIVER", connectionInfo.getJdbcDriver()),
                () -> Assertions.assertEquals("URL", connectionInfo.getJdbcUrl()),
                () -> Assertions.assertEquals("USERNAME", connectionInfo.getJdbcUsername()),
                () -> Assertions.assertEquals("PASSWORD", connectionInfo.getJdbcPassword())
        );
    }
}
