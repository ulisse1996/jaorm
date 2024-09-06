package io.github.ulisse1996.jaorm.extension.cdi;

import io.github.ulisse1996.jaorm.spi.QueriesService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import java.util.Set;

public class JakartaDaoProvider implements Extension {

    void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        Set<Class<?>> queries = QueriesService.getInstance().getQueries().keySet();
        for (Class<?> dao : queries) {
            event.addBean()
                    .addTransitiveTypeClosure(dao)
                    .beanClass(dao)
                    .scope(ApplicationScoped.class)
                    .produceWith((instance) -> QueriesService.getInstance().getQuery(dao));
        }
    }
}
