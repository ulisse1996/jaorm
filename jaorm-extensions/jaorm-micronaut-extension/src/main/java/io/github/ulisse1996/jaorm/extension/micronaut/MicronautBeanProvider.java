package io.github.ulisse1996.jaorm.extension.micronaut;

import io.github.ulisse1996.jaorm.spi.BeanProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MicronautBeanProvider extends BeanProvider {

    @Override
    public <T> T getBean(Class<T> aClass) {
        return ContextHolder.getContext().getBean(aClass);
    }

    @Override
    public <T> List<T> getBeans(Class<T> bean) {
        return new ArrayList<>(ContextHolder.getContext().getBeansOfType(bean));
    }

    @Override
    public <T> Optional<T> getOptBean(Class<T> bean) {
        return getBeans(bean).stream().findFirst();
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
