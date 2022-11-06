package io.github.ulisse1996.jaorm.extension.micronaut;

import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.micronaut.context.BeanContext;

import java.util.Objects;

public class ContextHolder {

    private static final Singleton<BeanContext> HOLDER = Singleton.instance();

    private ContextHolder() {}

    public static BeanContext getContext() {
        return Objects.requireNonNull(HOLDER.get());
    }

    public static void setContext(BeanContext context) {
        if (HOLDER.isPresent()) {
            throw new UnsupportedOperationException("Can't reset context !");
        }
        HOLDER.set(context);
    }
}
