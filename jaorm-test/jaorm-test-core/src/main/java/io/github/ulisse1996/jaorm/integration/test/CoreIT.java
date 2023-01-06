package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.Sort;
import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.EntityComparator;
import io.github.ulisse1996.jaorm.entity.Page;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.integration.test.entity.*;
import io.github.ulisse1996.jaorm.integration.test.query.AutoGenDao;
import io.github.ulisse1996.jaorm.integration.test.query.ProgressiveDao;
import io.github.ulisse1996.jaorm.integration.test.query.UserDAO;
import io.github.ulisse1996.jaorm.spi.FeatureConfigurator;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("java:S100")
public abstract class CoreIT extends AbstractIT {

    private final UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);

    // CRUD - Read

    @Test
    void should_read_user() {
        User user = getDefault();

        User found = userDAO.readByKey(99);
        assertSame(user, found);
        assertTotalInvocations(1);
    }

    @Test
    void should_read_opt_user() {
        User user = getDefault();

        Optional<User> opt = userDAO.readOptByKey(99);
        assertPresentAndSame(user, opt);
        assertTotalInvocations(1);
    }

    @Test
    void should_read_all_users() {
        User user = getDefault();

        List<User> list = userDAO.readAll();
        Assertions.assertFalse(list.isEmpty());
        assertSame(List.of(user), list);
        assertTotalInvocations(1);
    }

    @Test
    void should_read_page() {
        User user = getDefault();

        Page<User> page = userDAO.page(0, 10, Collections.singletonList(Sort.desc(UserColumns.USER_ID))); // 1 Only count
        Assertions.assertFalse(page.hasNext());
        Assertions.assertEquals(1, page.getCount());

        assertTotalInvocations(1);

        List<User> data = page.getData(); // 2
        Assertions.assertEquals(1, data.size());
        assertSame(user, data.get(0));

        assertTotalInvocations(2);
    }

    // CRUD - Update

    @Test
    void should_update_user() {
        User user = userDAO.readByKey(99); // 1
        user.setName("NEW_NAME");

        userDAO.update(user); // 2

        assertPresentAndSame(user, userDAO.readOptByKey(99)); // 3
        assertTotalInvocations(3);
    }

    @Test
    void should_update_with_batch() {
        Pair<User, User> users = createPair();

        userDAO.insert(List.of(users.getKey(), users.getValue())); // 2

        users.getKey().setName("NAME_11");
        users.getValue().setName("NAME_22");

        userDAO.updateWithBatch(List.of(users.getKey(), users.getValue())); // 3

        assertPresentAndSame(users.getKey(), userDAO.readOptByKey(1)); // 4
        assertPresentAndSame(users.getValue(), userDAO.readOptByKey(1)); // 5
        assertTotalInvocations(5);
    }

    // CRUD - Delete

    @Test
    void should_delete_user() {
        User user = userDAO.readByKey(99); // 1

        userDAO.delete(user); // 2

        Assertions.assertFalse(userDAO.readOptByKey(99).isPresent()); // 3
        assertTotalInvocations(3);
    }

    // CURD - Create

    @Test
    void should_insert_user() {
        User user = new User();
        user.setId(1);
        user.setName("NAME");

        userDAO.insert(user);

        Optional<User> opt = userDAO.readOptByKey(1);

        assertPresentAndSame(user, opt);
        assertTotalInvocations(2);
    }

    @Test
    void should_insert_with_batch() {
        Pair<User, User> users = createPair();

        userDAO.insertWithBatch(List.of(users.getKey(), users.getValue())); // 1

        assertPresentAndSame(users.getKey(), userDAO.readOptByKey(1)); // 2
        assertPresentAndSame(users.getValue(), userDAO.readOptByKey(1)); // 3

        assertTotalInvocations(3);
    }

    @Test
    public void should_insert_with_auto_generated() {
        AutoGenDao autoGenDao = QueriesService.getInstance().getQuery(AutoGenDao.class);

        AutoGenerated autoGenerated = new AutoGenerated();
        autoGenerated.setName("NAME_1");

        AutoGenerated autoGenerated2 = new AutoGenerated();
        autoGenerated2.setName("NAME_2");

        AutoGenerated autoGenerated3 = new AutoGenerated();
        autoGenerated3.setName("NAME_3");

        List<AutoGenerated> results = autoGenDao.insertWithBatch(List.of(autoGenerated, autoGenerated2, autoGenerated3)); // 1

        Assertions.assertNotNull(results);
        Assertions.assertFalse(results.isEmpty());
        Assertions.assertTrue(
                results.stream()
                        .map(AutoGenerated::getColGen)
                        .noneMatch(Objects::isNull)
        );

        assertTotalInvocations(1);
    }

    @Test
    public void should_return_all_generated_columns() {
        EntityWithProgressive p = new EntityWithProgressive();
        p.setValue("222");
        ProgressiveDao dao = QueriesService.getInstance().getQuery(ProgressiveDao.class);
        p = dao.insert(p); // 1

        Assertions.assertNotNull(p.getId());
        Assertions.assertEquals(BigDecimal.ONE, p.getId());
        Assertions.assertNotNull(p.getProgressive());
        Assertions.assertEquals(BigDecimal.valueOf(23), p.getProgressive());

        EntityWithProgressive pr = new EntityWithProgressive();
        pr.setId(p.getId());

        pr = dao.read(pr); // 2

        Assertions.assertTrue(EntityComparator.getInstance(EntityWithProgressive.class).equals(pr, p));

        assertTotalInvocations(2);
    }

    // CRUD - Upsert

    @Test
    void should_do_insert_for_missing_update() {
        User user = createPair().getKey();

        Assertions.assertTrue(FeatureConfigurator.getInstance().isInsertAfterFailedUpdateEnabled());

        userDAO.update(user); // 2 upsert

        assertPresentAndSame(user, userDAO.readOptByKey(1)); // 3

        assertTotalInvocations(3);
    }

    // Graphs

    @Test
    void should_get_full_user() {
        User user = createGraph();

        userDAO.insert(user); // 3 User, UserRole and UserSpecific

        Optional<User> result = User.USER_FULL.fetchOpt(createPair().getKey()); // 4

        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().getUserSpecific().isPresent());
        Assertions.assertEquals(1, result.get().getRoles().size());
        assertSame(user, result.get());
        assertTotalInvocations(4);
    }

    //@Test Should be available when we implement sub graphs
    void should_get_full_user_with_roles() {
        User user = createGraph();
        Role role = new Role();
        role.setRoleName("NAME");
        role.setRoleId(1);
        user.getRoles().get(0).setRole(role);

        userDAO.insert(user); // 4 User, UserRole, Role and UserSpecific

        Optional<User> result = User.USER_FULL_WITH_ROLES.fetchOpt(createPair().getKey()); // 5

        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().getUserSpecific().isPresent());
        Assertions.assertEquals(1, result.get().getRoles().size());
        Assertions.assertNotNull(result.get().getRoles().get(0).getRole());
        assertSame(user, result.get());
        assertTotalInvocations(5);
    }

    // Utils

    @NotNull
    private User createGraph() {
        User user = createPair().getKey();
        UserRole role = new UserRole();
        role.setRoleId(1);
        role.setUserId(1);
        UserSpecific specific = new UserSpecific();
        specific.setSpecificId(1);
        specific.setUserId(1);
        user.setUserSpecific(Result.of(specific));
        user.setRoles(Collections.singletonList(role));
        return user;
    }

    private Pair<User, User> createPair() {
        User user = new User();
        user.setId(1);
        user.setName("NAME_1");

        User user2 = new User();
        user2.setId(2);
        user2.setName("NAME_2");

        return new Pair<>(user, user2);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertPresentAndSame(User user, Optional<User> opt) {
        Assertions.assertTrue(opt.isPresent());
        assertSame(user, opt.get());
    }

    private void assertSame(List<User> users, List<User> others) {
        EntityComparator.getInstance(User.class)
                .equals(users, others);
    }

    private void assertSame(User user, User other) {
        EntityComparator.getInstance(User.class)
                .equals(user, other);
    }

    @NotNull
    private User getDefault() {
        User user = new User();
        user.setId(99);
        user.setName("NAME_99");
        user.setDepartmentId(99);
        return user;
    }
}
