package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.EntityComparator;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.exception.JaormValidationException;
import io.github.ulisse1996.jaorm.integration.test.entity.*;
import io.github.ulisse1996.jaorm.integration.test.projection.MyProjection;
import io.github.ulisse1996.jaorm.integration.test.projection.ProjectionDao;
import io.github.ulisse1996.jaorm.integration.test.query.*;
import io.github.ulisse1996.jaorm.integration.test.util.ExceptionLogger;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ExtendWith(ExceptionLogger.class)
class CoreIT extends AbstractIT {

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_map_result_set_with_stream(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        City city = new City();
        city.setCityId(2);
        city.setName("22");

        CityDAO cityDAO = QueriesService.getInstance().getQuery(CityDAO.class);
        cityDAO.insert(city);

        City found = QueryRunner.getSimple().readStream("SELECT * FROM CITY", Collections.emptyList())
                .map(m -> {
                    try {
                        return m.mapRow(rs -> {
                            City city1 = new City();
                            city1.setName(rs.getString("CITY_NAME"));
                            city1.setCityId(rs.getInt("CITY_ID"));
                            return city1;
                        });
                    } catch (Exception ex) {
                        Assertions.fail();
                        return null;
                    }
                }).collect(Collectors.toList()).get(0);
        Assertions.assertEquals(city, found);
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_cascade_with_opt(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        CascadeEntityInner inner = new CascadeEntityInner();
        inner.setCascadeId(1);
        inner.setName("NAME_INNER");
        CascadeEntity c = new CascadeEntity();
        c.setCascadeId(1);
        c.setName("NAME");
        c.setCascadeInnerEntity(Result.of(inner));

        CascadeDAO cascadeDAO = QueriesService.getInstance().getQuery(CascadeDAO.class);
        CascadeInnerDAO cascadeInnerDAO = QueriesService.getInstance().getQuery(CascadeInnerDAO.class);

        cascadeDAO.insert(c);

        // Should insert with cascade

        CascadeEntity found = cascadeDAO.findById(1);
        Assertions.assertTrue(found.getCascadeInnerEntity().isPresent());

        found.getCascadeInnerEntity().ifPresent(ce -> ce.setName("NAME_INNER_MODIFIED"));


        // Should update with cascade
        cascadeDAO.update(found);
        found = cascadeDAO.findById(1);
        Assertions.assertTrue(found.getCascadeInnerEntity().isPresent());
        Assertions.assertEquals("NAME_INNER_MODIFIED", found.getCascadeInnerEntity().get().getName());

        // Should delete with cascade

        cascadeDAO.delete(found);
        Assertions.assertThrows(JaormSqlException.class, () -> cascadeDAO.findById(1));
        Assertions.assertFalse(cascadeInnerDAO.findById(1).isPresent());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_cascade_with_simple_entity(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        BaseDao<City> cityDao = QueriesService.getInstance().getBaseDao(City.class);
        BaseDao<Store> storeDao = QueriesService.getInstance().getBaseDao(Store.class);

        City city = new City();
        city.setCityId(10);
        city.setName("NAME");

        cityDao.insert(city);
        city = cityDao.read(city);
        Assertions.assertTrue(city.getStores().isEmpty());
        Assertions.assertTrue(storeDao.readAll().isEmpty());

        city.getStores().add(new Store());
        city.getStores().get(0).setCityId(10);
        city.getStores().get(0).setName("NAME_2");
        city.getStores().get(0).setStoreId(1);

        cityDao.update(city);

        List<Store> stores = storeDao.readAll();
        Assertions.assertFalse(stores.isEmpty());
        Assertions.assertTrue(EntityComparator.getInstance(Store.class).equals(city.getStores().get(0), stores.get(0)));
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_read_all_mapped_fields(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);
        CompoundEntity compoundEntity = new CompoundEntity();
        compoundEntity.setCompoundId(1);
        compoundEntity = QueriesService.getInstance().getQuery(CompoundEntityDAO.class)
                .read(compoundEntity);
        Assertions.assertNotNull(compoundEntity.getCreateDate());
        Assertions.assertNotNull(compoundEntity.getCreationUser());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_return_all_generated_columns(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        EntityWithProgressive p = new EntityWithProgressive();
        p.setValue("222");
        ProgressiveDao dao = QueriesService.getInstance().getQuery(ProgressiveDao.class);
        p = dao.insert(p);

        Assertions.assertNotNull(p.getId());
        Assertions.assertEquals(BigDecimal.ONE, p.getId());
        Assertions.assertNotNull(p.getProgressive());
        Assertions.assertEquals(BigDecimal.valueOf(23), p.getProgressive());

        EntityWithProgressive pr = new EntityWithProgressive();
        pr.setId(p.getId());

        pr = dao.read(pr);

        Assertions.assertTrue(EntityComparator.getInstance(EntityWithProgressive.class).equals(pr, p));
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_generate_columns_with_table(HSQLDBProvider.DatabaseType type, String initSql) throws SQLException {
        setDataSource(type, initSql);

        QueryRunner.getSimple()
                .read("SELECT SEQ_VAL FROM SEQ_TABLE WHERE ID_SEQ = 'ENTITY'", Collections.emptyList())
                .mapRow(row -> {
                    Assertions.assertEquals(BigDecimal.ONE, row.getBigDecimal(1));
                    return null;
                });

        SequenceDao dao = QueriesService.getInstance().getQuery(SequenceDao.class);

        Sequence sequence = new Sequence();
        sequence.setVal("VAL");

        Sequence generated = dao.insert(sequence);

        Assertions.assertEquals(BigDecimal.ONE, generated.getId());
        Assertions.assertEquals(BigDecimal.valueOf(2), generated.getSeq());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_read_projections(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        ProjectionDao projectionDao = QueriesService.getInstance().getQuery(ProjectionDao.class);

        MyProjection projection = projectionDao.getMyProjection();
        Optional<MyProjection> optionalMyProjection = projectionDao.getOptMyProjection();
        List<MyProjection> myProjections = projectionDao.getAllMyProjection();

        Assertions.assertNotNull(projection);
        Assertions.assertEquals("SUB_NAME", projection.getSubName());
        Assertions.assertEquals(BigDecimal.ONE, projection.getId());
        Assertions.assertTrue(projection.isValid());
        Assertions.assertNull(projection.getOther());

        Assertions.assertTrue(optionalMyProjection.isPresent());
        Assertions.assertEquals("SUB_NAME", optionalMyProjection.get().getSubName());
        Assertions.assertEquals(BigDecimal.ONE, optionalMyProjection.get().getId());
        Assertions.assertTrue(optionalMyProjection.get().isValid());
        Assertions.assertNull(optionalMyProjection.get().getOther());

        Assertions.assertEquals(1, myProjections.size());
        Assertions.assertEquals("SUB_NAME", myProjections.get(0).getSubName());
        Assertions.assertEquals(BigDecimal.ONE, myProjections.get(0).getId());
        Assertions.assertTrue(myProjections.get(0).isValid());
        Assertions.assertNull(myProjections.get(0).getOther());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_insert_with_batch(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        StoreDAO storeDAO = QueriesService.getInstance().getQuery(StoreDAO.class);

        Store store = new Store();
        store.setStoreId(1);
        store.setName("NAME_1");
        store.setCityId(1);

        Store store2 = new Store();
        store2.setStoreId(2);
        store2.setName("NAME_2");
        store2.setCityId(2);

        Store store3 = new Store();
        store3.setStoreId(3);
        store3.setName("NAME_3");
        store3.setCityId(3);

        List<Store> results = storeDAO.insertWithBatch(Arrays.asList(store, store2, store3));
        Assertions.assertNotNull(results);
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_insert_with_auto_generated(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        AutoGenDao autoGenDao = QueriesService.getInstance().getQuery(AutoGenDao.class);

        AutoGenerated autoGenerated = new AutoGenerated();
        autoGenerated.setName("NAME_1");

        AutoGenerated autoGenerated2 = new AutoGenerated();
        autoGenerated2.setName("NAME_2");

        AutoGenerated autoGenerated3 = new AutoGenerated();
        autoGenerated3.setName("NAME_3");

        List<AutoGenerated> results = autoGenDao.insertWithBatch(Arrays.asList(autoGenerated, autoGenerated2, autoGenerated3));
        Assertions.assertNotNull(results);
        Assertions.assertFalse(results.isEmpty());
        Assertions.assertTrue(
                results.stream()
                        .map(AutoGenerated::getColGen)
                        .noneMatch(Objects::isNull)
        );
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_update_with_batch(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        StoreDAO storeDAO = QueriesService.getInstance().getQuery(StoreDAO.class);

        Store store = new Store();
        store.setStoreId(1);
        store.setName("NAME_1");

        Store store2 = new Store();
        store2.setStoreId(2);
        store2.setName("NAME_2");

        List<Store> insert = storeDAO.insert(Arrays.asList(store, store2))
                .stream()
                .sorted(Comparator.comparing(Store::getStoreId))
                .collect(Collectors.toList());
        insert.get(0).setName("NAME_1_MOD");
        insert.get(1).setName("NAME_2_MOD");

        List<Store> updated = storeDAO.updateWithBatch(insert);
        updated.sort(Comparator.comparing(Store::getStoreId));
        Assertions.assertEquals(insert, updated);
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_insert_with_batch_and_nested_relationships(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        City city = new City();
        city.setCityId(10);
        city.setName("CITY");
        city.setStores(Collections.singletonList(new Store()));
        city.getStores().get(0).setName("STORE");
        city.getStores().get(0).setCityId(10);
        city.getStores().get(0).setStoreId(30);
        city.getStores().get(0).setSellers(
                IntStream.range(0, 5)
                        .mapToObj(i -> {
                            Seller seller = new Seller();
                            seller.setName("SELLER_" + i);
                            seller.setId(i);
                            seller.setStoreId(30);
                            return seller;
                        }).collect(Collectors.toList())
        );

        City city2 = new City();
        city2.setCityId(11);
        city2.setName("CITY");
        city2.setStores(Collections.singletonList(new Store()));
        city2.getStores().get(0).setName("STORE");
        city2.getStores().get(0).setCityId(11);
        city2.getStores().get(0).setStoreId(31);
        city2.getStores().get(0).setSellers(
                IntStream.range(0, 5)
                        .mapToObj(i -> {
                            Seller seller = new Seller();
                            seller.setName("SELLER_" + (i * 2));
                            seller.setId(i * 2);
                            seller.setStoreId(31);
                            return seller;
                        }).collect(Collectors.toList())
        );

        CityDAO cityDAO = QueriesService.getInstance().getQuery(CityDAO.class);
        cityDAO.insertWithBatch(Arrays.asList(city, city2));

        City result = new City();
        result.setCityId(10);
        result = cityDAO.read(result);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.getStores().isEmpty());
        Assertions.assertFalse(result.getStores().get(0).getSellers().isEmpty());

        City result2 = new City();
        result2.setCityId(10);
        result2 = cityDAO.read(result2);

        Assertions.assertNotNull(result2);
        Assertions.assertFalse(result2.getStores().isEmpty());
        Assertions.assertFalse(result2.getStores().get(0).getSellers().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_return_empty_result_for_entity_from_graph(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        User user = new User();
        user.setId(2);
        Optional<User> userOpt = User.USER_FULL.fetchOpt(user);
        Assertions.assertFalse(userOpt.isPresent());
        Assertions.assertEquals(
                "SELECT user_0.USER_ID AS \"user_0.USER_ID\", user_0.USER_NAME AS \"user_0.USER_NAME\", user_0.DEPARTMENT_ID AS \"user_0.DEPARTMENT_ID\", user_role_1.USER_ID AS \"user_role_1.USER_ID\", user_role_1.ROLE_ID AS \"user_role_1.ROLE_ID\", user_specific_2.USER_ID AS \"user_specific_2.USER_ID\", user_specific_2.SPECIFIC_ID AS \"user_specific_2.SPECIFIC_ID\" FROM USER_ENTITY user_0 JOIN USER_ROLE user_role_1 ON user_0.USER_ID = user_role_1.USER_ID LEFT JOIN USER_SPECIFIC user_specific_2 ON user_0.USER_ID = user_specific_2.USER_ID WHERE user_0.USER_ID = ? ",
                getLastExecuted()
        );
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_return_result_for_entity_from_graph_without_opt(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        UserDAO userDao = QueriesService.getInstance().getQuery(UserDAO.class);
        UserRoleDAO rolesDao = QueriesService.getInstance().getQuery(UserRoleDAO.class);

        User user = new User();
        user.setId(2);
        user.setName("NAME");
        List<UserRole> roles = createRoles(5, 2);

        userDao.insert(user);
        rolesDao.insert(roles);

        user = new User();
        user.setId(2);
        Optional<User> userOpt = User.USER_FULL.fetchOpt(user);
        Assertions.assertTrue(userOpt.isPresent());
        Assertions.assertEquals(
                "SELECT user_0.USER_ID AS \"user_0.USER_ID\", user_0.USER_NAME AS \"user_0.USER_NAME\", user_0.DEPARTMENT_ID AS \"user_0.DEPARTMENT_ID\", user_role_1.USER_ID AS \"user_role_1.USER_ID\", user_role_1.ROLE_ID AS \"user_role_1.ROLE_ID\", user_specific_2.USER_ID AS \"user_specific_2.USER_ID\", user_specific_2.SPECIFIC_ID AS \"user_specific_2.SPECIFIC_ID\" FROM USER_ENTITY user_0 JOIN USER_ROLE user_role_1 ON user_0.USER_ID = user_role_1.USER_ID LEFT JOIN USER_SPECIFIC user_specific_2 ON user_0.USER_ID = user_specific_2.USER_ID WHERE user_0.USER_ID = ? ",
                getLastExecuted()
        );
        user = userOpt.get();
        Assertions.assertEquals(2, user.getId());
        Assertions.assertEquals("NAME", user.getName());
        Assertions.assertNotNull(user.getRoles());
        Assertions.assertEquals(5, user.getRoles().size());
        Assertions.assertFalse(user.getUserSpecific().isPresent());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_return_result_for_entity_from_graph_with_opt(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        UserDAO userDao = QueriesService.getInstance().getQuery(UserDAO.class);
        UserRoleDAO rolesDao = QueriesService.getInstance().getQuery(UserRoleDAO.class);
        UserSpecificDAO userSpecificDAO = QueriesService.getInstance().getQuery(UserSpecificDAO.class);

        User user = new User();
        user.setId(3);
        user.setName("NAME");
        List<UserRole> roles = createRoles(4, 3);
        UserSpecific specific = new UserSpecific();
        specific.setSpecificId(10);
        specific.setUserId(3);

        userDao.insert(user);
        rolesDao.insert(roles);
        userSpecificDAO.insert(specific);

        user = new User();
        user.setId(3);
        Optional<User> userOpt = User.USER_FULL.fetchOpt(user);
        Assertions.assertTrue(userOpt.isPresent());
        Assertions.assertEquals(
                "SELECT user_0.USER_ID AS \"user_0.USER_ID\", user_0.USER_NAME AS \"user_0.USER_NAME\", user_0.DEPARTMENT_ID AS \"user_0.DEPARTMENT_ID\", user_role_1.USER_ID AS \"user_role_1.USER_ID\", user_role_1.ROLE_ID AS \"user_role_1.ROLE_ID\", user_specific_2.USER_ID AS \"user_specific_2.USER_ID\", user_specific_2.SPECIFIC_ID AS \"user_specific_2.SPECIFIC_ID\" FROM USER_ENTITY user_0 JOIN USER_ROLE user_role_1 ON user_0.USER_ID = user_role_1.USER_ID LEFT JOIN USER_SPECIFIC user_specific_2 ON user_0.USER_ID = user_specific_2.USER_ID WHERE user_0.USER_ID = ? ",
                getLastExecuted()
        );
        user = userOpt.get();
        Assertions.assertEquals(3, user.getId());
        Assertions.assertEquals("NAME", user.getName());
        Assertions.assertNotNull(user.getRoles());
        Assertions.assertEquals(4, user.getRoles().size());
        Assertions.assertTrue(user.getUserSpecific().isPresent());
        Assertions.assertEquals(10, user.getUserSpecific().get().getSpecificId());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_validate_entity(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        ValidatedEntity entity = new ValidatedEntity();
        entity.setVal1(BigDecimal.TEN);
        entity.setDate(new Date()); // past date

        ValidatedEntityDAO dao = QueriesService.getInstance().getQuery(ValidatedEntityDAO.class);
        try {
            dao.insert(entity);
        } catch (JaormValidationException ex) {
            Assertions.assertEquals(2, ex.getResults().size());
            return;
        }

        Assertions.fail("Should throw JaormValidationException !");
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_use_auto_set_for_cascade(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        SchoolDao schoolDao = QueriesService.getInstance().getQuery(SchoolDao.class);

        School school = new School();
        school.setSchoolId(BigDecimal.ONE);
        school.setStudents(new ArrayList<>());
        school.getStudents().add(new Student());
        school.getStudents().add(new Student());
        school.getStudents().get(0).setStudentId(BigDecimal.TEN);
        school.getStudents().get(1).setStudentId(BigDecimal.valueOf(11));

        school = schoolDao.insert(school);

        for (Student student : school.getStudents()) {
            Assertions.assertEquals(BigDecimal.ONE, student.getSchoolId());
            Assertions.assertEquals("MY_NAME", student.getName());
        }
    }

    private List<UserRole> createRoles(int times, int ref) {
        return IntStream.range(0, times)
                .mapToObj(i -> {
                    UserRole role = new UserRole();
                    role.setRoleId(i);
                    role.setUserId(ref);
                    return role;
                }).collect(Collectors.toList());
    }
}
