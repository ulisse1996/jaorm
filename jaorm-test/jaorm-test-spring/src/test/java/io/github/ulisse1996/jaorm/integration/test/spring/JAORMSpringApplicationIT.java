package io.github.ulisse1996.jaorm.integration.test.spring;

import io.github.ulisse1996.jaorm.integration.test.spring.entity.SpringEntity;
import io.github.ulisse1996.jaorm.integration.test.spring.repository.JAORMSpringRepository;
import io.github.ulisse1996.jaorm.integration.test.spring.service.SpringTransactionalService;
import io.github.ulisse1996.jaorm.spi.BeanProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class JAORMSpringApplicationIT {

    @Autowired private JAORMSpringRepository repository;
    @Autowired private SpringTransactionalService transactionalService;

    @Test
    void should_create_spring_entity() {
        SpringEntity entity = new SpringEntity();
        entity.setCol1("COL1");
        entity.setCol2("COL2");
        repository.insert(entity);

        Assertions.assertTrue(repository.readOptByKey("COL1").isPresent());
    }

    @Test
    void should_use_spring_transaction() {
        try {
            SpringEntity entity = new SpringEntity();
            entity.setCol1("COL1");
            entity.setCol2("COL2");
            transactionalService.insertAndThrow(entity);
        } catch (Exception ex) {
            Assertions.assertFalse(transactionalService.readOpt("COL1").isPresent());
        }
    }

    @Test
    void should_create_bean_using_bean_provider() {
        BeanProvider provider = BeanProvider.getInstance();
        Assertions.assertNotNull(provider.getBean(SpringTransactionalService.class));
    }
}
