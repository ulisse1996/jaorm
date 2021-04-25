package io.jaorm.custom;

import io.jaorm.ServiceFinder;

import java.util.Optional;

public class CustomFeatures {

    private CustomFeatures() {}

    public static final CustomFeature<LikeFeature> LIKE_FEATURE = new DefaultFeature<>(LikeFeature.class);
    public static final CustomFeature<SqlAccessorFeature> SQL_ACCESSOR = new DefaultFeature<>(SqlAccessorFeature.class);

    /* Only for test*/ static class DefaultFeature<T> implements  CustomFeature<T> {

        private final T instance;
        private final boolean enabled;

        public DefaultFeature(Class<T> featureClass) {
            T feature;
            boolean featureCheck;
            try {
                feature = Optional.ofNullable(ServiceFinder.loadService(featureClass))
                    .orElseThrow(IllegalArgumentException::new);
                featureCheck = true;
            } catch (IllegalArgumentException ex) {
                feature = null;
                featureCheck = false;
            }
            this.instance = feature;
            this.enabled = featureCheck;
        }

        @Override
        public T getFeature() {
            if (isEnabled()) {
                return instance;
            }
            throw new UnsupportedOperationException("Feature not supported !");
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
