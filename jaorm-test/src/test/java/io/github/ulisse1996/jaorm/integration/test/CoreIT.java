package io.github.ulisse1996.jaorm.integration.test;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.EntityComparator;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.integration.test.entity.*;
import io.github.ulisse1996.jaorm.integration.test.query.CascadeDAO;
import io.github.ulisse1996.jaorm.integration.test.query.CascadeInnerDAO;
import io.github.ulisse1996.jaorm.integration.test.query.CityDAO;
import io.github.ulisse1996.jaorm.integration.test.query.CompoundEntityDAO;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
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

    @Test
    void should_read_all_mapped_fields() {
        CompoundEntity compoundEntity = new CompoundEntity();
        compoundEntity.setCompoundId(1);
        compoundEntity = QueriesService.getInstance().getQuery(CompoundEntityDAO.class)
                .read(compoundEntity);
        Assertions.assertNotNull(compoundEntity.getCreateDate());
        Assertions.assertNotNull(compoundEntity.getCreationUser());
    }
}
