package io.github.ulisse1996.jaorm.spi.provider;

import io.github.ulisse1996.jaorm.entity.GenerationInfo;

import java.util.List;

public interface GeneratorProvider {

    Class<?> getEntityClass();
    List<GenerationInfo> getInfo();
}
