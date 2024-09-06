package io.github.ulisse1996.jaorm.integration.test.query;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.integration.test.entity.Reservation;

@Dao
public interface ReservationDao extends BaseDao<Reservation> {
}
