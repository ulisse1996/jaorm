package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.QueryBuilder;
import io.github.ulisse1996.jaorm.dsl.query.common.MergeEnd;
import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.dsl.query.impl.SelectedImpl;
import io.github.ulisse1996.jaorm.entity.EntityComparator;
import io.github.ulisse1996.jaorm.entity.Page;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.integration.test.entity.*;
import io.github.ulisse1996.jaorm.integration.test.projection.UsernameProjection;
import io.github.ulisse1996.jaorm.integration.test.query.RoleDAO;
import io.github.ulisse1996.jaorm.integration.test.query.UserDAO;
import io.github.ulisse1996.jaorm.integration.test.query.UserRoleDAO;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.vendor.AnsiFunctions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("java:S100")
public abstract class QueryBuilderIT extends AbstractIT {

    @Test
    void should_not_found_user() {
        Optional<User> treeOpt = QueryBuilder.select(User.class)
                .where(UserColumns.USER_ID).eq(1)
                .readOpt();
        Assertions.assertFalse(treeOpt.isPresent());
    }

    @Test
    void should_not_found_user_list() {
        List<User> treeList = QueryBuilder.select(User.class)
                .where(UserColumns.USER_ID).ne(999)
                .readAll();
        Assertions.assertTrue(treeList.isEmpty());
    }

    @Test
    void should_throw_exception_for_not_found_user() {
        Assertions.assertThrows(JaormSqlException.class, () -> QueryBuilder.select(User.class) // NOSONAR
                .where(UserColumns.USER_ID).eq(99)
                .read());
    }

    @Test
    void should_find_opt_user() {
        User expected = new User();
        expected.setDepartmentId(1);
        expected.setName("NAME");
        expected.setId(999);

        QueriesService.getInstance().getQuery(UserDAO.class).insert(expected);

        Optional<User> treeOpt = QueryBuilder.select(User.class)
                .where(UserColumns.USER_ID).eq(999)
                .readOpt();
        Assertions.assertTrue(treeOpt.isPresent());
        Assertions.assertTrue(EntityComparator.getInstance(User.class).equals(expected, treeOpt.get())); //NOSONAR
    }

    @Test
    void should_find_only_one_user() {
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

    @Test
    void should_find_single_user() {
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

    @Test
    void should_search_entity_with_like_statements() {
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
        Assertions.assertTrue(comparator.equals(user, foundLike.get())); //NOSONAR
        Assertions.assertTrue(foundNotLike.isPresent());
        Assertions.assertTrue(comparator.equals(user, foundNotLike.get())); //NOSONAR
        Assertions.assertFalse(notFound.isPresent());
    }

    @Test
    void should_find_all_roles_using_relationship() {
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

    @Test
    void should_find_connected_tables() {
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

    @Test
    void should_limit_read_all() {
        UserDAO userDao = QueriesService.getInstance().getQuery(UserDAO.class);

        User user1 = createUser(1);
        User user2 = createUser(2);
        User user3 = createUser(3);
        User user4 = createUser(4);
        User user5 = createUser(5);

        Assertions.assertTrue(userDao.readAll().isEmpty());

        userDao.insert(Arrays.asList(user1, user2, user3, user4, user5));

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

    @Test
    void should_insert_using_dsl() {
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
        Assertions.assertEquals(10, optUser.get().getId()); //NOSONAR
        Assertions.assertEquals("NAME", optUser.get().getName());
        Assertions.assertEquals(20, optUser.get().getDepartmentId());
    }

    @Test
    void should_update_using_dsl() {
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
        Assertions.assertEquals("CHANGE_USERNAME", optUser.get().getName()); //NOSONAR
        Assertions.assertEquals(25, optUser.get().getDepartmentId());
    }

    @Test
    void should_invalidate_inner_where() {
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
                .andWhere(UserColumns.USER_NAME).eq((String) null).or(UserColumns.USER_NAME).eq((String) null);
        SelectedImpl<User, ?> impl = (SelectedImpl<User, ?>) eq;

        Assertions.assertEquals( "SELECT USER_ENTITY.USER_ID, USER_ENTITY.USER_NAME, USER_ENTITY.DEPARTMENT_ID FROM USER_ENTITY WHERE (USER_ENTITY.USER_ID = ?)", impl.asString(false));

        Assertions.assertTrue(
                eq.readOpt().isPresent()
        );
    }

    @Test
    void should_get_page_from_dsl() {
        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);

        Page<User> userPage = QueryBuilder.select(User.class)
                        .page(1, 10);
        Assertions.assertEquals(0, userPage.getCount());

        List<User> users = IntStream.range(0, 12)
                .mapToObj(i -> {
                    User user = new User();
                    user.setId(i);
                    user.setName("NAME_" + i);
                    return user;
                }).collect(Collectors.toList());
        userDAO.insertWithBatch(users);

        userPage = QueryBuilder.select(User.class)
                        .orderBy(OrderType.ASC, UserColumns.USER_ID)
                        .page(0, 10);

        Assertions.assertEquals(12, userPage.getCount());
        Assertions.assertEquals(10, userPage.getData().size());
        for (int i = 0; i < 10; i++) {
            User exp = users.get(i);
            User res = userPage.getData().get(i);
            Assertions.assertEquals(exp.getId(), res.getId());
            Assertions.assertEquals(exp.getName(), res.getName());
        }
        Assertions.assertFalse(userPage.hasPrevious());
        Assertions.assertTrue(userPage.hasNext());

        Optional<Page<User>> optionalUserPage = userPage.getNext();
        Assertions.assertTrue(optionalUserPage.isPresent());
        Assertions.assertEquals(2, optionalUserPage.get().getData().size()); //NOSONAR
    }

    @Test
    void should_update_using_function() {
        

        User user = new User();
        user.setId(10);
        user.setName("name");

        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);

        userDAO.insert(user);

        QueryBuilder.update(User.class)
                .setting(UserColumns.USER_NAME).usingFunction(AnsiFunctions.upper(UserColumns.USER_NAME))
                .execute();

        Optional<User> optUser = userDAO.readOpt(user);
        Assertions.assertTrue(optUser.isPresent());
        Assertions.assertEquals(user.getName().toUpperCase(), optUser.get().getName()); //NOSONAR
    }


    @Test
    void should_merge_entity() {

        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);

        User user = new User();
        user.setId(10);
        user.setName("NAME");

        User updateUser = new User();
        updateUser.setId(10);
        updateUser.setName("NAME_NEW");

        MergeEnd<User> merge = QueryBuilder.merge(User.class)
                .using(UserColumns.USER_ID, 10)
                .onEquals(UserColumns.USER_ID)
                .notMatchInsert(user)
                .matchUpdate(updateUser);

        Assertions.assertFalse(userDAO.readOptByKey(10).isPresent());

        merge.execute(); // Should do insert

        Optional<User> opt = userDAO.readOptByKey(10);
        Assertions.assertTrue(opt.isPresent());

        User optF = opt.get();
        Assertions.assertEquals(user.getName(), optF.getName());

        merge.execute(); // Should do update

        opt = userDAO.readOptByKey(10);
        Assertions.assertTrue(opt.isPresent());

        optF = opt.get();
        Assertions.assertEquals(updateUser.getName(), optF.getName());
    }

    @Test
    void should_read_using_simple_join() {
        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);
        UserRoleDAO userRoleDAO = QueriesService.getInstance().getQuery(UserRoleDAO.class);

        User user = createUser(1);
        UserRole role = new UserRole();
        role.setRoleId(3);
        role.setUserId(1);

        userDAO.insert(user);
        userRoleDAO.insert(role);

        Optional<User> optUser = QueryBuilder.select(User.class)
                .join(UserRole.class, "B").on(UserRoleColumns.USER_ID).eq(1)
                .where(UserRoleColumns.ROLE_ID, "B").eq(3)
                .readOpt();

        Assertions.assertTrue(optUser.isPresent());
        Assertions.assertEquals(1, optUser.get().getId());
        Assertions.assertEquals("NAME_1", optUser.get().getName());
    }

    @Test
    void should_read_upper_user_name() {
        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);
        User user = createUser(1);

        userDAO.insert(user);

        Optional<UsernameProjection> projection = QueryBuilder.select(UserColumns.USER_NAME)
                .from(UserColumns.TABLE_NAME)
                .where(AnsiFunctions.upper(UserColumns.USER_NAME)).eq("NAME_1")
                .readOpt(UsernameProjection.class);

        Assertions.assertTrue(projection.isPresent());
        Assertions.assertEquals("NAME_1", projection.get().getName());
    }

    @Test
    void should_search_upper_name_returning_lower_name() {
        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);
        User user = createUser(1);

        userDAO.insert(user);

        Optional<UsernameProjection> projection = QueryBuilder.select(AnsiFunctions.lower(UserColumns.USER_NAME).as("USER_NAME"))
                .from(UserColumns.TABLE_NAME)
                .where(AnsiFunctions.upper(UserColumns.USER_NAME)).eq("NAME_1")
                .readOpt(UsernameProjection.class);

        Assertions.assertTrue(projection.isPresent());
        Assertions.assertEquals("name_1", projection.get().getName());
    }

    private User createUser(int i) {
        User user = new User();
        user.setId(i);
        user.setName(String.format("NAME_%d", i));
        return user;
    }
}
