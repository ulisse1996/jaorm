package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.Sort;
import io.github.ulisse1996.jaorm.entity.EntityComparator;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Page;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.integration.test.entity.*;
import io.github.ulisse1996.jaorm.integration.test.projection.MyProjection;
import io.github.ulisse1996.jaorm.integration.test.projection.ProjectionDao;
import io.github.ulisse1996.jaorm.integration.test.query.*;
import io.github.ulisse1996.jaorm.mapping.RowMapper;
import io.github.ulisse1996.jaorm.mapping.TableRow;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("java:S100")
public abstract class QueryIT extends AbstractIT {

    @Test
    void should_create_new_entity() {
        UserDAO dao = QueriesService.getInstance().getQuery(UserDAO.class);

        User user = new User();
        user.setId(1);

        User inserted = dao.insert(user);
        Assertions.assertTrue(inserted instanceof EntityDelegate);

        User found = dao.read(user);
        Assertions.assertNotSame(user, found);
        Assertions.assertEquals(user.getId(), found.getId());
    }

    @Test
    void should_update_all_entity() {
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
                .peek(t -> t.setDepartmentId(t.getId() + 10)) //NOSONAR
                .collect(Collectors.toList());

        dao.update(modUsers);

        users = dao.readAll();
        Assertions.assertEquals(modUsers, users);
    }

    @Test
    void should_delete_entity() {
        User user = new User();
        user.setId(1);
        user.setName("NAME");
        user.setDepartmentId(1);

        UserDAO dao = QueriesService.getInstance().getQuery(UserDAO.class);

        user = dao.insert(user);
        Optional<User> optionalUser = dao.readOpt(user);

        Assertions.assertTrue(optionalUser.isPresent());
        Assertions.assertTrue(EntityComparator.getInstance(User.class).equals(user, optionalUser.get())); //NOSONAR

        dao.delete(user);

        optionalUser = dao.readOpt(user);
        Assertions.assertFalse(optionalUser.isPresent());
    }

    @Test
    void should_do_cascade_operation() {
        CityDAO cityDAO = QueriesService.getInstance().getQuery(CityDAO.class);

        City city = new City();
        city.setCityId(10);
        city.setName("CITY");

        Store store = new Store();
        store.setStoreId(1);
        store.setName("NAME1");
        store.setCityId(city.getCityId());

        city.setStores(Collections.singletonList(store));

        cityDAO.insert(city);

        List<Store> stores = QueriesService.getInstance().getQuery(StoreDAO.class).readAll();
        Assertions.assertEquals(1, stores.size());
        Assertions.assertTrue(EntityComparator.getInstance(Store.class).equals(store, stores.get(0)));

        // Now read and update

        city = cityDAO.read(city);
        city.getStores()
                .forEach(s -> s.setName(s.getName() + "_AFTER"));
        cityDAO.update(city);

        stores = QueriesService.getInstance().getQuery(StoreDAO.class).readAll();
        Assertions.assertEquals(1, stores.size());
        Assertions.assertTrue(stores.get(0).getName().endsWith("_AFTER"));
    }

    @Test
    void should_check_unique_with_distinct() {
        City city = new City();
        city.setCityId(1);
        city.setName("CITY");

        City city2 = new City();
        city2.setCityId(2);
        city2.setName("CITY");

        City city3 = new City();
        city3.setCityId(3);
        city3.setName("CITY");

        CityDAO cityDAO = QueriesService.getInstance().getQuery(CityDAO.class);

        cityDAO.insert(Arrays.asList(city, city2, city3));

        List<City> cities = cityDAO.readAll();
        Assertions.assertEquals(3, cities.size());

        cities = cities.stream()
                .sorted(Comparator.comparing(City::getCityId))
                .filter(EntityComparator.distinct(City::getName))
                .collect(Collectors.toList());

        Assertions.assertEquals(1, cities.size());
        Assertions.assertTrue(EntityComparator.getInstance(City.class).equals(city, cities.get(0)));
    }

    @Test
    void should_insert_entity_with_auto_generated_key() {
        AutoGenerated generated = new AutoGenerated();
        generated.setName("NAME");
        AutoGenDao query = QueriesService.getInstance().getQuery(AutoGenDao.class);
        generated = query.insert(generated);
        Assertions.assertEquals("NAME", generated.getName());
        Assertions.assertEquals(BigDecimal.ONE, generated.getColGen());
    }

    @Test
    void should_use_custom_sql_accessor() {
        CustomAccessorDAO dao = QueriesService.getInstance().getQuery(CustomAccessorDAO.class);
        try {
            dao.select(CustomAccessor.MyEnumCustom.VAL);
        } catch (Exception ex) {
            Assertions.fail("Should not throw exception", ex);
        }
    }

    @Test
    void should_get_optional_entity() {
        User user = new User();
        user.setId(1);

        UserDAO dao = QueriesService.getInstance().getQuery(UserDAO.class);
        dao.insert(user);

        user = dao.read(user);

        Assertions.assertFalse(user.getUserSpecific().isPresent());
    }

    @Test
    void should_create_query_using_file() {
        CustomUserDao query = QueriesService.getInstance()
                .getQuery(CustomUserDao.class);
        Optional<User> userOpt = query.getUserOpt(1);
        Assertions.assertFalse(userOpt.isPresent());
    }

    @Test
    void should_create_and_use_correct_table_row_mapping() throws SQLException {
        User user = getUser(1);
        UserDAO dao = QueriesService.getInstance().getQuery(UserDAO.class);
        TableRowUserDao rowDao = QueriesService.getInstance().getQuery(TableRowUserDao.class);

        dao.insert(user);

        RowMapper<User> userRowMapper = rs -> {
            User mapped = new User();
            mapped.setId(rs.getInt("USER_ID"));
            mapped.setName(rs.getString("USER_NAME"));
            return mapped;
        };

        try (TableRow row = rowDao.readById(1)){
            User mapped = row.mapRow(userRowMapper);
            Assertions.assertTrue(
                    EntityComparator.getInstance(User.class)
                        .equals(user, mapped)
            );
        }

        List<User> users = rowDao.readStreamById(1)
                .map(row -> {
                    try {
                        return row.mapRow(userRowMapper);
                    } catch (SQLException ex) {
                        throw new JaormSqlException(ex);
                    }
                }).collect(Collectors.toList());

        Assertions.assertFalse(users.isEmpty());
        Assertions.assertTrue(
                EntityComparator.getInstance(User.class)
                        .equals(user, users.get(0))
        );

        Optional<User> userOpt = rowDao.readByIdOpt(1)
                .map(row -> {
                    try {
                        return row.mapRow(userRowMapper);
                    } catch (SQLException ex) {
                        throw new JaormSqlException(ex);
                    }
                });

        Assertions.assertTrue(userOpt.isPresent());
        Assertions.assertTrue(
                EntityComparator.getInstance(User.class)
                        .equals(user, userOpt.get()) //NOSONAR
        );
    }

    @Test
    void should_map_stream_with_projection() {
        ProjectionDao projectionDao = QueriesService.getInstance().getQuery(ProjectionDao.class);

        List<MyProjection> myProjections = projectionDao.getAllStream()
                .collect(Collectors.toList());

        Assertions.assertEquals(1, myProjections.size());
        Assertions.assertEquals("SUB_NAME", myProjections.get(0).getSubName());
        Assertions.assertEquals(BigDecimal.ONE, myProjections.get(0).getId());
        Assertions.assertTrue(myProjections.get(0).isValid());
        Assertions.assertNull(myProjections.get(0).getOther());
    }

    @Test
    void should_get_page_from_dao() {
        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);

        Page<User> userPage = userDAO.page(0, 10, Collections.emptyList());
        Assertions.assertEquals(0, userPage.getCount());

        List<User> users = IntStream.range(0, 12)
                .mapToObj(i -> {
                    User user = new User();
                    user.setId(i);
                    user.setName("NAME_" + i);
                    return user;
                }).collect(Collectors.toList());
        userDAO.insertWithBatch(users);

        userPage = userDAO.page(0, 10, Collections.singletonList(Sort.asc(UserColumns.USER_ID)));

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

    private User getUser(int i) {
        User user = new User();
        user.setId(i);
        user.setName("USER_" + i);
        return user;
    }
}
