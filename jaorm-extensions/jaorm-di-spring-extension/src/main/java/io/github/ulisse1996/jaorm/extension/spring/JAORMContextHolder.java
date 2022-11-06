package io.github.ulisse1996.jaorm.extension.spring;

import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class JAORMContextHolder {

    private static final Singleton<JAORMContextHolder> HOLDER = Singleton.instance();
    private final ApplicationContext context;

    private JAORMContextHolder(ApplicationContext context) {
        this.context = context;
    }

    public static JAORMContextHolder init(ApplicationContext context) {
        JAORMContextHolder contextHolder = new JAORMContextHolder(context);
        HOLDER.set(contextHolder);
        return contextHolder;
    }

    public static JAORMContextHolder getInstance() {
        return HOLDER.get();
    }

    public ApplicationContext getContext() {
        return this.context;
    }
}
