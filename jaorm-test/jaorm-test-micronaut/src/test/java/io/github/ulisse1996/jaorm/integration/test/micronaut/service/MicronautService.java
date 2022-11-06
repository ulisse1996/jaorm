package io.github.ulisse1996.jaorm.integration.test.micronaut.service;

import io.github.ulisse1996.jaorm.integration.test.micronaut.entity.MicronautEntity;
import io.github.ulisse1996.jaorm.integration.test.micronaut.entity.MicronautRepository;
import jakarta.inject.Singleton;

import javax.transaction.Transactional;

@Singleton
@Transactional(rollbackOn = IllegalArgumentException.class)
public class MicronautService {

    private final MicronautRepository repository;

    public MicronautService(MicronautRepository repository) {
        this.repository = repository;
    }

    public void insertAndThrow(MicronautEntity entity) {
        repository.insert(entity);
        throw new IllegalArgumentException();
    }
}
