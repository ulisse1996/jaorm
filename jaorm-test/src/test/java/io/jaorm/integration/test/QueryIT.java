package io.jaorm.integration.test;

import io.jaorm.entity.EntityComparator;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.QueriesService;
import io.jaorm.integration.test.entity.User;
import io.jaorm.integration.test.query.UserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class QueryIT extends AbstractIT {

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_create_new_entity(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        UserDAO dao = QueriesService.getInstance().getQuery(UserDAO.class);

        User user = new User();
        user.setId(1);

        User inserted = dao.insert(user);
        Assertions.assertTrue(inserted instanceof EntityDelegate);

        User found = dao.read(user);
        Assertions.assertNotSame(user, found);
        Assertions.assertEquals(user.getId(), found.getId());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_update_all_entity(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        User user = getUser(1);
        User user2 = getUser(2);
        User user3 = getUser(3);
        User user4 = getUser(4);
        User user5 = getUser(5);

        UserDAO dao = QueriesService.getInstance().getQuery(UserDAO.class);

        dao.insert(Arrays.asList(user, user2, user3, user4, user5));

        List<User> users = dao.readAll();

        Assertions.assertEquals(5, users.size());
        users.sort(Comparator.comparing(User::getId));

        Assertions.assertEquals(users.get(0), user);
        Assertions.assertEquals(users.get(1), user2);
        Assertions.assertEquals(users.get(2), user3);
        Assertions.assertEquals(users.get(3), user4);
        Assertions.assertEquals(users.get(4), user5);

        List<User> modUsers = users.stream()
                .peek(t -> t.setDepartmentId(t.getId() + 10))
                .collect(Collectors.toList());

        dao.update(modUsers);

        users = dao.readAll();
        Assertions.assertEquals(modUsers, users);
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_delete_entity(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        User user = new User();
        user.setId(1);
        user.setName("NAME");
        user.setDepartmentId(1);

        UserDAO dao = QueriesService.getInstance().getQuery(UserDAO.class);

        user = dao.insert(user);
        Optional<User> optionalUser = dao.readOpt(user);

        Assertions.assertTrue(optionalUser.isPresent());
        Assertions.assertTrue(EntityComparator.getInstance(User.class).equals(user, optionalUser.get()));

        dao.delete(user);

        optionalUser = dao.readOpt(user);
        Assertions.assertFalse(optionalUser.isPresent());
    }

    private User getUser(int i) {
        User user = new User();
        user.setId(i);
        user.setName("USER_" + i);
        return user;
    }
}
