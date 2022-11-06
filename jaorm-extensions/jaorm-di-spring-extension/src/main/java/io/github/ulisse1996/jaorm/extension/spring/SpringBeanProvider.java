package io.github.ulisse1996.jaorm.extension.spring;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.spi.BeanProvider;
import org.springframework.beans.BeansException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SpringBeanProvider extends BeanProvider {

    private static final JaormLogger logger = JaormLogger.getLogger(SpringBeanProvider.class);

    @Override
    public <T> T getBean(Class<T> bean) {
        return JAORMContextHolder.getInstance().getContext().getBean(bean);
    }

    @Override
    public <T> List<T> getBeans(Class<T> bean) {
        try {
            return new ArrayList<>(JAORMContextHolder.getInstance().getContext().getBeansOfType(bean).values());
        } catch (BeansException ex) {
            logger.error(() -> String.format("Can't create bean for type %s: %s", bean.getName(), ex), ex);
            return Collections.emptyList();
        }
    }

    @Override
    public <T> Optional<T> getOptBean(Class<T> bean) {
        List<T> beans = getBeans(bean);
        return beans.stream().findFirst();
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
