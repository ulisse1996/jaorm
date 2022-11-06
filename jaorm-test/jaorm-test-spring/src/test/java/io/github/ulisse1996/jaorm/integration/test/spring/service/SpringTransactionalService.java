package io.github.ulisse1996.jaorm.integration.test.spring.service;

import io.github.ulisse1996.jaorm.integration.test.spring.entity.SpringEntity;
import io.github.ulisse1996.jaorm.integration.test.spring.repository.JAORMSpringRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(rollbackFor = IllegalArgumentException.class)
@Service
public class SpringTransactionalService {

    private final JAORMSpringRepository dao;

    public SpringTransactionalService(JAORMSpringRepository dao) {
        this.dao = dao;
    }

    public Optional<SpringEntity> readOpt(String col1) {
        SpringEntity entity = new SpringEntity();
        entity.setCol1(col1);
        return dao.readOpt(entity);
    }

    public void insertAndThrow(SpringEntity entity) {
        dao.insert(entity);
        throw new IllegalArgumentException();
    }
}
