package io.github.ulisse1996.jaorm.extension.cdi;

import io.github.ulisse1996.jaorm.spi.BeanProvider;

import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaxBeanProvider extends BeanProvider {

    @Override
    public <T> T getBean(Class<T> bean) {
        return CDI.current().select(bean).get();
    }

    @Override
    public <T> List<T> getBeans(Class<T> bean) {
        return StreamSupport.stream(CDI.current().select(bean).spliterator(), false)
                .collect(Collectors.toList());
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
