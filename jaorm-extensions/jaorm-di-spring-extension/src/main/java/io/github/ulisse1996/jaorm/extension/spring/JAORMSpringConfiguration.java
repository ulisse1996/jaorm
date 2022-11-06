package io.github.ulisse1996.jaorm.extension.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JAORMSpringConfiguration {

    @Bean
    public JAORMContextHolder holder(ApplicationContext context) {
        return JAORMContextHolder.init(context);
    }
}
