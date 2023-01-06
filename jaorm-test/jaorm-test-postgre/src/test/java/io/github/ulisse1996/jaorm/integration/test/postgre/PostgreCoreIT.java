package io.github.ulisse1996.jaorm.integration.test.postgre;

import io.github.ulisse1996.jaorm.integration.test.CoreIT;
import io.github.ulisse1996.jaorm.integration.test.postgre.dao.PostgisEntityDao;
import io.github.ulisse1996.jaorm.integration.test.postgre.entity.PostgisEntity;
import io.github.ulisse1996.jaorm.spatial.Geography;
import io.github.ulisse1996.jaorm.spi.QueriesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class PostgreCoreIT extends CoreIT {

    @Override
    public void afterInit() throws Exception {
        super.afterInit();
        execute("INSERT INTO POSTGIS_ENTITY(ID, GEOG) VALUES(2, ST_GeomFromText('POINT(-71.060316 48.432044)', 4326))");
    }

    @Test
    void should_create_entity_with_geography() {
        PostgisEntity entity = new PostgisEntity();
        entity.setId(1);
        entity.setGeography(new Geography(45.54033960700574, 10.22779665255048));
        PostgisEntityDao dao = QueriesService.getInstance().getQuery(PostgisEntityDao.class);

        dao.insert(entity);

        entity = dao.read(entity);

        Geography geography = entity.getGeography();
        Assertions.assertEquals(45.54033960700574, geography.getLatitude());
        Assertions.assertEquals(10.22779665255048, geography.getLongitude());
        Assertions.assertEquals(4326, geography.getSrid());
    }

    @Test
    void should_read_entity_with_geography() {
        PostgisEntity entity = new PostgisEntity();
        entity.setId(2);
        PostgisEntityDao dao = QueriesService.getInstance().getQuery(PostgisEntityDao.class);
        entity = dao.read(entity);

        Geography geography = entity.getGeography();
        Assertions.assertEquals(-71.060316, geography.getLatitude());
        Assertions.assertEquals(48.432044, geography.getLongitude());
        Assertions.assertEquals(4326, geography.getSrid());
    }
}
