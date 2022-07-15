package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.integration.test.entity.User;
import io.github.ulisse1996.jaorm.integration.test.query.UserDAO;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

@SuppressWarnings("java:S100")
public abstract class TransactionalIT extends AbstractIT {

    @Test
    void should_do_rollback_for_exception() {
        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                Transactional.exec(() -> {
                    User user = new User();
                    user.setName("NAME");
                    user.setId(2);

                    userDAO.insert(user);

                    throw new IllegalArgumentException();
                }, IllegalArgumentException.class));

        User user = new User();
        user.setId(2);
        Optional<User> notFound = userDAO.readOpt(user);
        Assertions.assertFalse(notFound.isPresent());
    }
}
