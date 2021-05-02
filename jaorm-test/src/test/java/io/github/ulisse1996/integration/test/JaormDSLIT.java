package io.github.ulisse1996.integration.test;

import io.github.ulisse1996.dsl.Jaorm;
import io.github.ulisse1996.dsl.common.LikeType;
import io.github.ulisse1996.integration.test.entity.*;
import io.github.ulisse1996.integration.test.query.RoleDAO;
import io.github.ulisse1996.integration.test.query.UserDAO;
import io.github.ulisse1996.integration.test.query.UserRoleDAO;
import io.github.ulisse1996.entity.EntityComparator;
import io.github.ulisse1996.integration.test.entity.*;
import io.github.ulisse1996.spi.QueriesService;
import io.github.ulisse1996.exception.JaormSqlException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class JaormDSLIT extends AbstractIT {

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_not_found_user(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Optional<User> treeOpt = Jaorm.select(User.class)
                .where(UserColumns.USER_ID).eq(1)
                .readOpt();
        Assertions.assertFalse(treeOpt.isPresent());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_not_found_user_list(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        List<User> treeList = Jaorm.select(User.class)
                .where(UserColumns.USER_ID).ne(999)
                .readAll();
        Assertions.assertTrue(treeList.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_throw_exception_for_not_found_user(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Assertions.assertThrows(JaormSqlException.class, () -> Jaorm.select(User.class) // NOSONAR
                .where(UserColumns.USER_ID).eq(99)
                .read());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_find_opt_user(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        User expected = new User();
        expected.setDepartmentId(1);
        expected.setName("NAME");
        expected.setId(999);

        QueriesService.getInstance().getQuery(UserDAO.class).insert(expected);

        Optional<User> treeOpt = Jaorm.select(User.class)
                .where(UserColumns.USER_ID).eq(999)
                .readOpt();
        Assertions.assertTrue(treeOpt.isPresent());
        Assertions.assertTrue(EntityComparator.getInstance(User.class).equals(expected, treeOpt.get()));
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_find_only_one_user(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        User expected = new User();
        expected.setDepartmentId(1);
        expected.setName("NAME");
        expected.setId(999);

        QueriesService.getInstance().getQuery(UserDAO.class).insert(expected);

        List<User> treeList = Jaorm.select(User.class)
                .where(UserColumns.USER_ID).eq(999)
                .readAll();
        Assertions.assertEquals(1, treeList.size());
        Assertions.assertTrue(EntityComparator.getInstance(User.class).equals(expected, treeList.get(0)));
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_find_single_user(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        User expected = new User();
        expected.setDepartmentId(1);
        expected.setName("NAME");
        expected.setId(999);

        QueriesService.getInstance().getQuery(UserDAO.class).insert(expected);

        User result = Jaorm.select(User.class)
                .where(UserColumns.USER_ID).eq(999)
                .read();
        Assertions.assertTrue(EntityComparator.getInstance(User.class).equals(expected, result));
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_search_entity_with_like_statements(HSQLDBProvider.DatabaseType databaseType, String initSql) {
        setDataSource(databaseType, initSql);

        User user = new User();
        user.setId(1);
        user.setName("NAME");

        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);
        EntityComparator<User> comparator = EntityComparator.getInstance(User.class);
        userDAO.insert(user);

        Optional<User> foundLike = Jaorm.select(User.class)
                .where(UserColumns.USER_NAME).like(LikeType.FULL, "NAME")
                .readOpt();
        Optional<User> foundNotLike = Jaorm.select(User.class)
                .where(UserColumns.USER_NAME).notLike(LikeType.FULL, "FAL")
                .readOpt();
        Optional<User> notFound = Jaorm.select(User.class)
                .where(UserColumns.USER_NAME).like(LikeType.START, "ST")
                .readOpt();

        Assertions.assertTrue(foundLike.isPresent());
        Assertions.assertTrue(comparator.equals(user, foundLike.get()));
        Assertions.assertTrue(foundNotLike.isPresent());
        Assertions.assertTrue(comparator.equals(user, foundNotLike.get()));
        Assertions.assertFalse(notFound.isPresent());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_find_all_roles_using_relationship(HSQLDBProvider.DatabaseType databaseType, String initSql) {
        setDataSource(databaseType, initSql);

        User user = new User();
        user.setId(1);
        user.setName("NAME");

        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("NAME1");
        Role role2 = new Role();
        role2.setRoleId(2);
        role2.setRoleName("NAME2");

        UserRole userRole = new UserRole();
        userRole.setUserId(1);
        userRole.setRoleId(1);
        UserRole userRole2 = new UserRole();
        userRole2.setUserId(1);
        userRole2.setRoleId(2);

        UserDAO userDao = QueriesService.getInstance().getQuery(UserDAO.class);
        RoleDAO roleDAO = QueriesService.getInstance().getQuery(RoleDAO.class);
        UserRoleDAO userRoleDAO = QueriesService.getInstance().getQuery(UserRoleDAO.class);

        userDao.insert(user);
        roleDAO.insert(Arrays.asList(role, role2));
        userRoleDAO.insert(Arrays.asList(userRole, userRole2));

        User readUser = userDao.read(user);
        List<Role> founds = readUser.getRoles()
                .stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());
        Assertions.assertTrue(EntityComparator.getInstance(Role.class).equals(Arrays.asList(role, role2), founds));
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_find_connected_tables(HSQLDBProvider.DatabaseType databaseType, String initSql) {
        setDataSource(databaseType, initSql);

        UserDAO userDao = QueriesService.getInstance().getQuery(UserDAO.class);
        RoleDAO roleDAO = QueriesService.getInstance().getQuery(RoleDAO.class);
        UserRoleDAO userRoleDAO = QueriesService.getInstance().getQuery(UserRoleDAO.class);

        User user = new User();
        user.setId(2);
        user.setName("NAME");
        userDao.insert(user);

        Role role = new Role();
        role.setRoleName("ROLE");
        role.setRoleId(2);
        roleDAO.insert(role);

        UserRole userRole = new UserRole();
        userRole.setRoleId(2);
        userRole.setUserId(2);
        userRoleDAO.insert(userRole);

        User read = Jaorm.select(User.class)
                .join(UserRole.class)
                .on(UserColumns.USER_ID).eq(UserRoleColumns.USER_ID)
                .read();

        Assertions.assertTrue(
                EntityComparator.getInstance(User.class).equals(user, read)
        );
    }
}
