package io.github.ulisse1996.jaorm.dsl.generator;

import io.github.ulisse1996.jaorm.spi.DslService;

public class DslColumnSupport implements DslService {

    @Override
    public boolean isSupported() {
        return true;
    }
}
