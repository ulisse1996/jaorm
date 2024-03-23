package io.github.ulisse1996.jaorm.external;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.concurrent.locks.ReentrantLock;

public abstract class LombokSupport {

    private static final Singleton<LombokSupport> INSTANCE = Singleton.instance();
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static LombokSupport getInstance() {
        LOCK.lock();
        try {
            if (!INSTANCE.isPresent()) {
                try {
                    INSTANCE.set(ServiceFinder.loadService(LombokSupport.class));
                } catch (Exception ex) {
                    INSTANCE.set(NoOp.INSTANCE);
                }
            }
        } finally {
            LOCK.unlock();
        }

        return INSTANCE.get();
    }

    public abstract boolean isSupported();
    public abstract boolean isLombokGenerated(Element element);
    public abstract boolean hasLombokNoArgs(TypeElement entity);
    public abstract Element generateFakeElement(Element element, GenerationType generationType);

    public enum GenerationType {
        GETTER,
        SETTER
    }

    private static final class NoOp extends LombokSupport {

        private static final NoOp INSTANCE = new NoOp();

        @Override
        public boolean isSupported() {
            return false;
        }

        @Override
        public boolean isLombokGenerated(Element element) {
            return false;
        }

        @Override
        public boolean hasLombokNoArgs(TypeElement entity) {
            return false;
        }

        @Override
        public Element generateFakeElement(Element element, GenerationType generationType) {
            throw new UnsupportedOperationException("NoOp can't generate element !");
        }
    }
}
