package io.github.ulisse1996.jaorm.extension.quarkus;

import io.github.ulisse1996.jaorm.spi.FrameworkIntegrationService;
import io.github.ulisse1996.jaorm.util.ClassChecker;
import io.quarkus.runtime.LaunchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class QuarkusFrameworkIntegrationService extends FrameworkIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(QuarkusFrameworkIntegrationService.class);

    @Override
    public boolean isActive() {
        return LaunchMode.DEVELOPMENT.equals(LaunchMode.current());
    }

    @Override
    public boolean requireReInit(Set<Class<?>> classes) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for (Class<?> klass : classes) {
            Class<?> found = ClassChecker.findClass(klass.getName(), contextClassLoader);
            if (found == null || !found.equals(klass)) {
                logger.debug("Found missing/mismatched class, reloading service");
                return true;
            }
        }

        return false;
    }
}
