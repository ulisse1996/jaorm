package io.github.ulisse1996.jaorm.integration.test.micronaut;

import io.github.ulisse1996.jaorm.integration.test.micronaut.entity.MicronautEntity;
import io.github.ulisse1996.jaorm.integration.test.micronaut.entity.MicronautRepository;
import io.github.ulisse1996.jaorm.integration.test.micronaut.service.MicronautService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.jdbc.DelegatingDataSource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@MicronautTest
public class MicronautIT {

    @Inject private MicronautRepository repository;
    @Inject private DataSource dataSource;
    @Inject private MicronautService service;

    @BeforeEach
    void init() throws SQLException, URISyntaxException, IOException {
        URL resource = MicronautIT.class.getResource("/init.sql");
        List<String> strings = Files.readAllLines(Paths.get(Objects.requireNonNull(resource).toURI()));
        for (String s : strings) {
            try (Connection connection = DelegatingDataSource.unwrapDataSource(dataSource).getConnection();
                 PreparedStatement pr = connection.prepareStatement(s)) {
                pr.execute();
            }
        }
    }

    @Test
    void should_create_entity() {
        MicronautEntity entity = new MicronautEntity();
        entity.setEntityId("COL1");
        entity.setCol2("COL2");
        repository.insert(entity);
        Assertions.assertTrue(repository.readOpt(entity).isPresent());
    }

    @Test
    void should_not_save_entity_during_transaction_error() {
        MicronautEntity entity = new MicronautEntity();
        entity.setEntityId("VERY_LONG_STRING_THAT_CAUSES_ROLLBACK");
        entity.setCol2("COL2");
        try {
            service.insertAndThrow(entity);
        } catch (Exception ex) {
            Assertions.assertFalse(repository.readOpt(entity).isPresent());
        }
    }
}
