package io.github.ulisse1996.jaorm.extension.spring;

import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy
public class ThrowingInstance {

    public static final Singleton<Boolean> THROW_SINGLETON = Singleton.instance();

    public ThrowingInstance() {
        THROW_SINGLETON.set(true);
        throw new IllegalArgumentException();
    }
}
