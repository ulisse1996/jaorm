package io.github.ulisse1996.integration.test.spring;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(rollbackFor = IllegalArgumentException.class)
@Service
public class SpringTransactionalService {

    private final SpringSingletonDAO dao;

    public SpringTransactionalService(SpringSingletonDAO dao) {
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
