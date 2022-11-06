package io.github.ulisse1996.jaorm.spi;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public abstract class BeanProvider {

    private static final Singleton<BeanProvider> INSTANCE = Singleton.instance();

    public static synchronized BeanProvider getInstance() {
        if (!INSTANCE.isPresent()) {
            INSTANCE.set(
                    StreamSupport.stream(ServiceFinder.loadServices(BeanProvider.class).spliterator(), false)
                            .findFirst()
                            .orElse(BeanProvider.NoOp.INSTANCE)
            );
        }

        return INSTANCE.get();
    }

    public abstract <T> T getBean(Class<T> bean);
    public abstract <T> List<T> getBeans(Class<T> bean);
    public abstract <T> Optional<T> getOptBean(Class<T> bean);
    public abstract boolean isActive();

    static class NoOp extends BeanProvider {

        static final NoOp INSTANCE = new NoOp();

        @Override
        public <T> T getBean(Class<T> bean) {
            throw unsupported();
        }

        @Override
        public <T> List<T> getBeans(Class<T> bean) {
            throw unsupported();
        }

        @Override
        public <T> Optional<T> getOptBean(Class<T> bean) {
            throw unsupported();
        }

        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("NoOp implementation !");
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }
}
