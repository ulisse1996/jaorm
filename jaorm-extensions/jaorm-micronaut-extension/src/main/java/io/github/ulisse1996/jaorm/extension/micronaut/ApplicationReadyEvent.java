package io.github.ulisse1996.jaorm.extension.micronaut;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;

@Singleton
public class ApplicationReadyEvent {

    private static final JaormLogger logger = JaormLogger.getLogger(ApplicationReadyEvent.class);

    @EventListener
    public void onStartup(StartupEvent event) {
        ContextHolder.setContext(event.getSource());
        logger.info("Initialized JAORM Micronaut Context"::toString);
    }
}