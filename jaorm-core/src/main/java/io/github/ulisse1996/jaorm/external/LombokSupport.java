package io.github.ulisse1996.jaorm.external;

import io.github.ulisse1996.jaorm.ServiceFinder;
import io.github.ulisse1996.jaorm.spi.common.Singleton;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public abstract class LombokSupport {

    private static final Singleton<LombokSupport> INSTANCE = Singleton.instance();

    public static synchronized LombokSupport getInstance() {
        if (!INSTANCE.isPresent()) {
            try {
                INSTANCE.set(ServiceFinder.loadService(LombokSupport.class));
            } catch (Exception ex) {
                INSTANCE.set(NoOp.INSTANCE);
            }
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
