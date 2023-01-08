package io.github.ulisse1996.jaorm.extension.quarkus;

import io.github.ulisse1996.jaorm.util.ClassChecker;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ProfileManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

class QuarkusFrameworkIntegrationServiceTest {

    private final QuarkusFrameworkIntegrationService service = new QuarkusFrameworkIntegrationService();

    @Test
    void should_return_true_for_dev_mode() {
        try (MockedStatic<ProfileManager> mk = Mockito.mockStatic(ProfileManager.class)) {
            mk.when(ProfileManager::getLaunchMode).thenReturn(LaunchMode.DEVELOPMENT);
            Assertions.assertTrue(service.isActive());
        }
    }

    @Test
    void should_return_false_for_all_classes_found() {
        Assertions.assertFalse(service.requireReInit(Collections.singleton(Object.class)));
    }

    @Test
    void should_return_true_for_missing_class() {
        try (MockedStatic<ClassChecker> mk = Mockito.mockStatic(ClassChecker.class)) {
            mk.when(() -> ClassChecker.findClass(Mockito.eq(Object.class.getName()), Mockito.any()))
                    .thenReturn(null);
            Assertions.assertTrue(service.requireReInit(Collections.singleton(Object.class)));
        }
    }

    @Test
    void should_return_true_for_mismatch_class() {
        try (MockedStatic<ClassChecker> mk = Mockito.mockStatic(ClassChecker.class)) {
            mk.when(() -> ClassChecker.findClass(Mockito.eq(Object.class.getName()), Mockito.any()))
                    .thenReturn(Boolean.class);
            Assertions.assertTrue(service.requireReInit(Collections.singleton(Object.class)));
        }
    }
}