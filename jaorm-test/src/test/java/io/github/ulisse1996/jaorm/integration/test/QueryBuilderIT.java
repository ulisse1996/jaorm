package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.QueryBuilder;
import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.dsl.query.impl.SelectedImpl;
import io.github.ulisse1996.jaorm.entity.EntityComparator;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.integration.test.entity.*;
import io.github.ulisse1996.jaorm.integration.test.query.RoleDAO;
import io.github.ulisse1996.jaorm.integration.test.query.UserDAO;
import io.github.ulisse1996.jaorm.integration.test.query.UserRoleDAO;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class QueryBuilderIT extends AbstractIT {

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_not_found_user(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Optional<User> treeOpt = QueryBuilder.select(User.class)
                .where(UserColumns.USER_ID).eq(1)
                .readOpt();
        Assertions.assertFalse(treeOpt.isPresent());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_not_found_user_list(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        List<User> treeList = QueryBuilder.select(User.class)
                .where(UserColumns.USER_ID).ne(999)
                .readAll();
        Assertions.assertTrue(treeList.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_throw_exception_for_not_found_user(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Assertions.assertThrows(JaormSqlException.class, () -> QueryBuilder.select(User.class) // NOSONAR
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

        Optional<User> treeOpt = QueryBuilder.select(User.class)
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

        List<User> treeList = QueryBuilder.select(User.class)
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

        User result = QueryBuilder.select(User.class)
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

        Optional<User> foundLike = QueryBuilder.select(User.class)
                .where(UserColumns.USER_NAME).like(LikeType.FULL, "NAME")
                .readOpt();
        Optional<User> foundNotLike = QueryBuilder.select(User.class)
                .where(UserColumns.USER_NAME).notLike(LikeType.FULL, "FAL")
                .readOpt();
        Optional<User> notFound = QueryBuilder.select(User.class)
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

        User read = QueryBuilder.select(User.class)
                .join(UserRole.class).on(UserRoleColumns.USER_ID).eq(UserColumns.USER_ID)
                .read();

        Assertions.assertTrue(
                EntityComparator.getInstance(User.class).equals(user, read)
        );
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_limit_read_all(HSQLDBProvider.DatabaseType databaseType, String initSql) {
        setDataSource(databaseType, initSql);

        UserDAO userDao = QueriesService.getInstance().getQuery(UserDAO.class);

        User user1 = createUser(1);
        User user2 = createUser(2);
        User user3 = createUser(3);
        User user4 = createUser(4);
        User user5 = createUser(5);

        Assertions.assertTrue(userDao.readAll().isEmpty());

        userDao.insert(Arrays.asList(user1, user2, user3, user4, user5));

        try (MockedStatic<VendorSpecific> mk = Mockito.mockStatic(VendorSpecific.class)) {
            mk.when(() -> VendorSpecific.getSpecific(LimitOffsetSpecific.class))
                    .thenReturn(StandardOffSetLimitSpecific.INSTANCE);
            List<User> readAll = QueryBuilder.select(User.class)
                    .orderBy(OrderType.ASC, UserColumns.USER_ID)
                    .readAll();

            List<User> usersLimit = QueryBuilder.select(User.class)
                    .orderBy(OrderType.ASC, UserColumns.USER_ID)
                    .limit(3)
                    .readAll();

            List<User> usersLimitOffset = QueryBuilder.select(User.class)
                    .orderBy(OrderType.ASC, UserColumns.USER_ID)
                    .offset(2)
                    .limit(2)
                    .readAll();

            Assertions.assertAll(
                    () -> Assertions.assertEquals(5, readAll.size()),
                    () -> Assertions.assertEquals(3, usersLimit.size()),
                    () -> Assertions.assertEquals(1, usersLimit.get(0).getId()),
                    () -> Assertions.assertEquals(2, usersLimit.get(1).getId()),
                    () -> Assertions.assertEquals(3, usersLimit.get(2).getId()),
                    () -> Assertions.assertEquals(2, usersLimitOffset.size()),
                    () -> Assertions.assertEquals(3, usersLimitOffset.get(0).getId()),
                    () -> Assertions.assertEquals(4, usersLimitOffset.get(1).getId())
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_insert_using_dsl(HSQLDBProvider.DatabaseType databaseType, String initSql) {
        setDataSource(databaseType, initSql);

        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);

        User user = new User();
        user.setId(10);
        Optional<User> optUser = userDAO.readOpt(user);

        Assertions.assertFalse(optUser.isPresent());

        QueryBuilder.insertInto(User.class)
                .column(UserColumns.USER_ID).withValue(10)
                .column(UserColumns.USER_NAME).withValue("NAME")
                .column(UserColumns.DEPARTMENT_ID).withValue(20)
                .execute();

        optUser = userDAO.readOpt(user);

        Assertions.assertTrue(optUser.isPresent());
        Assertions.assertEquals(10, optUser.get().getId());
        Assertions.assertEquals("NAME", optUser.get().getName());
        Assertions.assertEquals(20, optUser.get().getDepartmentId());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_update_using_dsl(HSQLDBProvider.DatabaseType databaseType, String initSql) {
        setDataSource(databaseType, initSql);

        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);

        User user = new User();
        user.setId(15);
        user.setName("NAME");
        user.setDepartmentId(30);
        userDAO.insert(user);

        QueryBuilder.update(User.class)
                .setting(UserColumns.USER_NAME).toValue("CHANGE_USERNAME")
                .setting(UserColumns.DEPARTMENT_ID).toValue(25)
                .where(UserColumns.USER_NAME).eq("NAME")
                .andWhere(UserColumns.USER_ID).eq(15)
                .execute();

        Optional<User> optUser = userDAO.readOpt(user);
        Assertions.assertTrue(optUser.isPresent());
        Assertions.assertEquals("CHANGE_USERNAME", optUser.get().getName());
        Assertions.assertEquals(25, optUser.get().getDepartmentId());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_invalidate_inner_where(HSQLDBProvider.DatabaseType databaseType, String initSql) {
        setDataSource(databaseType, initSql);

        UserDAO userDao = QueriesService.getInstance().getQuery(UserDAO.class);

        User user = new User();
        user.setId(2);
        user.setName("NAME_2");
        userDao.insert(user);

        Assertions.assertTrue(
                userDao.readOpt(user).isPresent()
        );

        SelectedWhere<User> eq = QueryBuilder.select(
                User.class,
                QueryConfig.builder()
                        .withWhereChecker((column, operation, value) -> {
                            if (column.getType().equals(String.class)) {
                                return value != null && !((String) value).isEmpty();
                            }

                            return true;
                        })
                        .build()
        ).where(UserColumns.USER_ID).eq(2)
                .andWhere(UserColumns.USER_NAME).eq(null).or(UserColumns.USER_NAME).eq(null);
        SelectedImpl<User, ?> impl = (SelectedImpl<User, ?>) eq;

        Assertions.assertEquals( "SELECT USER_ENTITY.USER_ID, USER_ENTITY.USER_NAME, USER_ENTITY.DEPARTMENT_ID FROM USER_ENTITY WHERE (USER_ENTITY.USER_ID = ?)", impl.asString(false));

        Assertions.assertTrue(
                eq.readOpt().isPresent()
        );
    }

    private User createUser(int i) {
        User user = new User();
        user.setId(i);
        user.setName("NAME_i");
        return user;
    }

    private static class StandardOffSetLimitSpecific implements LimitOffsetSpecific {

        public static final StandardOffSetLimitSpecific INSTANCE = new StandardOffSetLimitSpecific();

        @Override
        public String convertOffSetLimitSupport(int limitRow) {
            return String.format(" LIMIT %d", limitRow);
        }

        @Override
        public String convertOffsetSupport(int offset) {
            return String.format(" OFFSET %d ", offset);
        }

        @Override
        public String convertOffSetLimitSupport(int limitRow, int offsetRow) {
            return String.format(" LIMIT %d OFFSET %d", limitRow, offsetRow);
        }
    }
}
