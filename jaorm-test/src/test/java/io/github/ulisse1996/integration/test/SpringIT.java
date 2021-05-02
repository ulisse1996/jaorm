package io.github.ulisse1996.integration.test;

import io.github.ulisse1996.integration.test.spring.*;
import io.github.ulisse1996.integration.test.spring.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringIT extends AbstractIT {

    @Autowired private SpringSingletonDAO singletonDAO;
    @Autowired private SpringPrototypeDAO prototypeDAO;
    @Autowired private SpringRequestDAO requestDAO;
    @Autowired private SpringSessionDAO sessionDAO;
    @Autowired private SpringApplicationDAO applicationDAO;
    @Autowired private SpringTransactionalService transactionalService;

    SpringIT() {
        super(false);
    }

    @Test
    void should_create_spring_beans() {
        Assertions.assertNotNull(singletonDAO);
        Assertions.assertNotNull(prototypeDAO);
        Assertions.assertNotNull(requestDAO);
        Assertions.assertNotNull(sessionDAO);
        Assertions.assertNotNull(applicationDAO);
    }

    @Test
    void should_return_same_entity_after_rollback() {
        setProvider(SpringTestApplication.PROVIDER);
        createDB("init.sql");
        SpringEntity entity = new SpringEntity();
        entity.setCol1("1");
        entity.setCol2("2");
        try {
            transactionalService.insertAndThrow(entity);
        } catch (IllegalArgumentException ex) {
            Assertions.assertFalse(transactionalService.readOpt("1").isPresent());
        }
    }
}
