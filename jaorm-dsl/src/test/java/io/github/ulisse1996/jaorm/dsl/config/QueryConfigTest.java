package io.github.ulisse1996.jaorm.dsl.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QueryConfigTest {

    @Test
    void should_create_a_custom_query_config() {
        QueryConfig config = QueryConfig.builder().withWhereChecker(new DefaultWhereChecker())
                .caseInsensitive()
                .build();
        Assertions.assertTrue(config.isCaseInsensitive());
        Assertions.assertTrue(config.getChecker() instanceof DefaultWhereChecker);
    }

    @Test
    void should_create_a_custom_query_config_with_case_sensitive() {
        QueryConfig config = QueryConfig.builder().withWhereChecker(new DefaultWhereChecker())
                .caseSensitive()
                .build();
        Assertions.assertFalse(config.isCaseInsensitive());
        Assertions.assertTrue(config.getChecker() instanceof DefaultWhereChecker);
    }
}
