package io.github.ulisse1996.jaorm.integration.test.postgre.dao;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.integration.test.postgre.entity.PostgisEntity;

@Dao
public interface PostgisEntityDao extends BaseDao<PostgisEntity> {
}
