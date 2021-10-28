package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.EntityComparator;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.generated.Tables;
import io.github.ulisse1996.jaorm.integration.test.entity.*;
import io.github.ulisse1996.jaorm.integration.test.projection.MyProjection;
import io.github.ulisse1996.jaorm.integration.test.projection.ProjectionDao;
import io.github.ulisse1996.jaorm.integration.test.query.*;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    void should_read_entity_using_tables(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Optional<City> cityOpt = Tables.CITY.cityId(10)
                .readOpt();

        City city = new City();
        city.setCityId(11);
        city.setName("NAME");


        Assertions.assertFalse(cityOpt.isPresent());

        QueriesService.getInstance().getQuery(CityDAO.class).insert(city);

        cityOpt = Tables.CITY.cityId(11)
                .readOpt();

        Assertions.assertTrue(cityOpt.isPresent());
        Assertions.assertTrue(EntityComparator.getInstance(City.class).equals(city, cityOpt.get()));
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
}
