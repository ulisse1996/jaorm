package io.jaorm.integration.test.query;

import io.jaorm.BaseDao;
import io.jaorm.integration.test.entity.Tree;
import io.jaorm.processor.annotation.Dao;

@Dao
public interface TreeDAO extends BaseDao<Tree> {}
