package io.github.ulisse1996.dsl.generator;

import io.github.ulisse1996.spi.DslService;

public class DslColumnSupport implements DslService {

    @Override
    public boolean isSupported() {
        return true;
    }
}
