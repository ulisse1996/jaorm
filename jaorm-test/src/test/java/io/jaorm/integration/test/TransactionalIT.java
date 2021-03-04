package io.jaorm.integration.test;

import io.jaorm.integration.test.entity.User;
import io.jaorm.integration.test.query.UserDAO;
import io.jaorm.spi.QueriesService;
import io.jaorm.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

class TransactionalIT extends AbstractIT {

    public TransactionalIT() {
        super(false, true);
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_do_rollback_for_exception(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

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
