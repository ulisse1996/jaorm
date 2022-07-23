package io.github.ulisse1996.jaorm.integration.test.spring;

import io.github.ulisse1996.jaorm.integration.test.AbstractIT;
import io.github.ulisse1996.jaorm.integration.test.spring.entity.SpringEntity;
import io.github.ulisse1996.jaorm.integration.test.spring.repository.SpringApplicationDAO;
import io.github.ulisse1996.jaorm.integration.test.spring.repository.SpringPrototypeDAO;
import io.github.ulisse1996.jaorm.integration.test.spring.repository.SpringRequestDAO;
import io.github.ulisse1996.jaorm.integration.test.spring.service.SpringTransactionalService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = {SpringIT.Initializer.class})
class SpringIT extends AbstractIT {

    @Autowired private SpringApplicationDAO applicationDAO;
    @Autowired private SpringPrototypeDAO prototypeDAO;
    @Autowired private SpringRequestDAO requestDAO;
    @Autowired private SpringTransactionalService transactionalService;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + SpringDatabaseInitializer.INSTANCE.get().getJdbcUrl(),
                    "spring.datasource.username=" + SpringDatabaseInitializer.INSTANCE.get().getUsername(),
                    "spring.datasource.password=" + SpringDatabaseInitializer.INSTANCE.get().getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }
    }

    @Test
    void should_create_spring_beans() {
        Assertions.assertNotNull(prototypeDAO);
        Assertions.assertNotNull(requestDAO);
        Assertions.assertNotNull(applicationDAO);
    }

    @Test
    void should_return_same_entity_after_rollback() {
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
