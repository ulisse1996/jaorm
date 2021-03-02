package io.jaorm.dsl.generator;

import io.jaorm.spi.DslService;

public class DslColumnSupport implements DslService {

    @Override
    public boolean isSupported() {
        return true;
    }
}
