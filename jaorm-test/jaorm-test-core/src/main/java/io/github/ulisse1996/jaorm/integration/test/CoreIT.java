package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.EntityComparator;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.exception.JaormValidationException;
import io.github.ulisse1996.jaorm.integration.test.entity.*;
import io.github.ulisse1996.jaorm.integration.test.projection.MyProjection;
import io.github.ulisse1996.jaorm.integration.test.projection.ProjectionDao;
import io.github.ulisse1996.jaorm.integration.test.query.*;
import io.github.ulisse1996.jaorm.mapping.Cursor;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("java:S100")
public abstract class CoreIT extends AbstractIT {

    protected static final String NAME_2 = "NAME_2";
    protected static final String SUB_NAME = "SUB_NAME";
    protected static final String NAME_1 = "NAME_1";

    @Test
    void should_map_result_set_with_stream() {
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

    @Test
    void should_cascade_with_opt() {
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

    @Test
    void should_cascade_with_simple_entity() {
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
        city.getStores().get(0).setName(NAME_2);
        city.getStores().get(0).setStoreId(1);

        cityDao.update(city);

        List<Store> stores = storeDao.readAll();
        Assertions.assertFalse(stores.isEmpty());
        Assertions.assertTrue(EntityComparator.getInstance(Store.class).equals(city.getStores().get(0), stores.get(0)));
    }

    @Test
    void should_read_all_mapped_fields() {
        CompoundEntity compoundEntity = new CompoundEntity();
        compoundEntity.setCompoundId(1);
        compoundEntity = QueriesService.getInstance().getQuery(CompoundEntityDAO.class)
                .read(compoundEntity);
        Assertions.assertNotNull(compoundEntity.getCreateDate());
        Assertions.assertNotNull(compoundEntity.getCreationUser());
    }

    @Test
    public void should_return_all_generated_columns() {
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

    @SuppressWarnings("resource")
    @Test
    void should_generate_columns_with_table() throws SQLException {
        

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

    @Test
    void should_read_projections() {
        ProjectionDao projectionDao = QueriesService.getInstance().getQuery(ProjectionDao.class);

        MyProjection projection = projectionDao.getMyProjection();
        Optional<MyProjection> optionalMyProjection = projectionDao.getOptMyProjection();
        List<MyProjection> myProjections = projectionDao.getAllMyProjection();

        Assertions.assertNotNull(projection);
        Assertions.assertEquals(SUB_NAME, projection.getSubName());
        Assertions.assertEquals(BigDecimal.ONE, projection.getId());
        Assertions.assertTrue(projection.isValid());
        Assertions.assertNull(projection.getOther());

        Assertions.assertTrue(optionalMyProjection.isPresent());
        Assertions.assertEquals(SUB_NAME, optionalMyProjection.get().getSubName()); //NOSONAR
        Assertions.assertEquals(BigDecimal.ONE, optionalMyProjection.get().getId());
        Assertions.assertTrue(optionalMyProjection.get().isValid());
        Assertions.assertNull(optionalMyProjection.get().getOther());

        Assertions.assertEquals(1, myProjections.size());
        Assertions.assertEquals(SUB_NAME, myProjections.get(0).getSubName());
        Assertions.assertEquals(BigDecimal.ONE, myProjections.get(0).getId());
        Assertions.assertTrue(myProjections.get(0).isValid());
        Assertions.assertNull(myProjections.get(0).getOther());
    }

    @Test
    void should_insert_with_batch() {
        StoreDAO storeDAO = QueriesService.getInstance().getQuery(StoreDAO.class);

        Store store = new Store();
        store.setStoreId(1);
        store.setName(NAME_1);
        store.setCityId(1);

        Store store2 = new Store();
        store2.setStoreId(2);
        store2.setName(NAME_2);
        store2.setCityId(2);

        Store store3 = new Store();
        store3.setStoreId(3);
        store3.setName("NAME_3");
        store3.setCityId(3);

        List<Store> results = storeDAO.insertWithBatch(Arrays.asList(store, store2, store3));
        Assertions.assertNotNull(results);
    }

    @Test
    public void should_insert_with_auto_generated() {
        AutoGenDao autoGenDao = QueriesService.getInstance().getQuery(AutoGenDao.class);

        AutoGenerated autoGenerated = new AutoGenerated();
        autoGenerated.setName(NAME_1);

        AutoGenerated autoGenerated2 = new AutoGenerated();
        autoGenerated2.setName(NAME_2);

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

    @Test
    void should_update_with_batch() {
        StoreDAO storeDAO = QueriesService.getInstance().getQuery(StoreDAO.class);

        Store store = new Store();
        store.setStoreId(1);
        store.setName(NAME_1);

        Store store2 = new Store();
        store2.setStoreId(2);
        store2.setName(NAME_2);

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

    @Test
    void should_insert_with_batch_and_nested_relationships() {
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

        City result = getCity(cityDAO);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.getStores().isEmpty());
        Assertions.assertFalse(result.getStores().get(0).getSellers().isEmpty());

        City result2 = getCity(cityDAO);

        Assertions.assertNotNull(result2);
        Assertions.assertFalse(result2.getStores().isEmpty());
        Assertions.assertFalse(result2.getStores().get(0).getSellers().isEmpty());
    }

    private City getCity(CityDAO cityDAO) {
        City result = new City();
        result.setCityId(10);
        result = cityDAO.read(result);
        return result;
    }

    @Test
    void should_return_result_for_entity_from_graph_without_opt() {
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

        user = userOpt.get(); //NOSONAR
        Assertions.assertEquals(2, user.getId());
        Assertions.assertEquals("NAME", user.getName());
        Assertions.assertNotNull(user.getRoles());
        Assertions.assertEquals(5, user.getRoles().size());
        Assertions.assertFalse(user.getUserSpecific().isPresent());
    }

    @Test
    void should_return_result_for_entity_from_graph_with_opt() {
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

        user = userOpt.get(); //NOSONAR
        Assertions.assertEquals(3, user.getId());
        Assertions.assertEquals("NAME", user.getName());
        Assertions.assertNotNull(user.getRoles());
        Assertions.assertEquals(4, user.getRoles().size());
        Assertions.assertTrue(user.getUserSpecific().isPresent());
        Assertions.assertEquals(10, user.getUserSpecific().get().getSpecificId());
    }

    @Test
    void should_validate_entity() {
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

    @Test
    void should_use_auto_set_for_cascade() {
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

    @Test
    void should_use_crud_operations_with_active_record() {
        Activity activity = new Activity();
        activity.setId(BigDecimal.ONE);
        activity.setName("NAME");
        activity.setDate(new Date());
        activity = activity.insert();

        Assertions.assertTrue(activity instanceof EntityDelegate);

        Date current = activity.getDate();

        activity.setDate(new Date());
        activity = activity.update();

        Assertions.assertNotEquals(current, activity.getDate());

        activity.delete();

        Assertions.assertFalse(
                activity.readOpt().isPresent()
        );
    }

    @Test
    void should_delete_school_with_all_students() {
        School school = new School();
        school.setSchoolId(BigDecimal.TEN);
        school.setAllStudents(
                Arrays.asList(
                        createStudent(1, "ST_NAME_1"),
                        createStudent(2, "ST_NAME_2")
                )
        );

        SchoolDao dao = QueriesService.getInstance().getQuery(SchoolDao.class);
        StudentDao studentDao = QueriesService.getInstance().getQuery(StudentDao.class);
        school = dao.insert(school);

        Assertions.assertEquals(2, studentDao.readAll().size());

        dao.delete(school);

        Assertions.assertEquals(0, dao.readAll().size());
        Assertions.assertEquals(0, studentDao.readAll().size());
    }

    @Test
    void should_read_using_cursor() {
        User user = new User();
        user.setId(1);
        user.setName("NAME");
        user.setRoles(createRoles(10, 1));

        UserDAO userDAO = QueriesService.getInstance().getQuery(UserDAO.class);
        UserRoleDAO roleDAO = QueriesService.getInstance().getQuery(UserRoleDAO.class);

        userDAO.insert(user);
        roleDAO.insert(user.getRoles());

        User fetched = userDAO.readByKey(1);
        try (Cursor<UserRole> rolesCursor = fetched.getRolesCursor()) {
            boolean found = false;
            for (UserRole role : rolesCursor) {
                if (role.getRoleId() == 5) {
                    found = true;
                    break;
                }
            }

            Assertions.assertTrue(found);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    private Student createStudent(int index, String name) {
        Student student = new Student();
        student.setName(name);
        student.setStudentId(BigDecimal.valueOf(index));
        return student;
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
