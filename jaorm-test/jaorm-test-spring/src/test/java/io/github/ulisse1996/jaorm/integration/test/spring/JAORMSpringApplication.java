package io.github.ulisse1996.jaorm.integration.test.spring;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@SpringBootApplication
public class JAORMSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(JAORMSpringApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(DataSource dataSource) {
        return args -> {
            URL resource = JAORMSpringApplication.class.getResource("/init.sql");
            List<String> strings = Files.readAllLines(Paths.get(Objects.requireNonNull(resource).toURI()));
            for (String s : strings) {
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement pr = connection.prepareStatement(s)) {
                    pr.execute();
                }
            }
        };
    }
}
