package io.jaorm.integration.test;

import io.jaorm.dsl.Jaorm;
import io.jaorm.entity.EntityComparator;
import io.jaorm.entity.QueriesService;
import io.jaorm.exception.JaormSqlException;
import io.jaorm.integration.test.entity.Tree;
import io.jaorm.integration.test.query.TreeDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;

class JaormDSLIT extends AbstractIT {

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_not_found_tree(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Optional<Tree> treeOpt = Jaorm.select(Tree.class)
                .where("TREE_ID").eq(1)
                .readOpt();
        Assertions.assertFalse(treeOpt.isPresent());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_not_found_tree_list(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        List<Tree> treeList = Jaorm.select(Tree.class)
                .where("TREE_ID").ne(999)
                .readAll();
        Assertions.assertTrue(treeList.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_throw_exception_for_not_found_tree(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Assertions.assertThrows(JaormSqlException.class, () -> Jaorm.select(Tree.class)
                .where("TREE_ID").eq(99)
                .read());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_find_opt_tree(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Tree expected = new Tree();
        expected.setFruitId(1);
        expected.setName("NAME");
        expected.setId(999);

        QueriesService.getInstance().getQuery(TreeDAO.class).insert(expected);

        Optional<Tree> treeOpt = Jaorm.select(Tree.class)
                .where("TREE_ID").eq(999)
                .readOpt();
        Assertions.assertTrue(treeOpt.isPresent());
        Assertions.assertTrue(EntityComparator.getInstance(Tree.class).equals(expected, treeOpt.get()));
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_find_only_one_tree(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Tree expected = new Tree();
        expected.setFruitId(1);
        expected.setName("NAME");
        expected.setId(999);

        QueriesService.getInstance().getQuery(TreeDAO.class).insert(expected);

        List<Tree> treeList = Jaorm.select(Tree.class)
                .where("TREE_ID").eq(999)
                .readAll();
        Assertions.assertEquals(1, treeList.size());
        Assertions.assertTrue(EntityComparator.getInstance(Tree.class).equals(expected, treeList.get(0)));
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_find_single_tree(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Tree expected = new Tree();
        expected.setFruitId(1);
        expected.setName("NAME");
        expected.setId(999);

        QueriesService.getInstance().getQuery(TreeDAO.class).insert(expected);

        Tree result = Jaorm.select(Tree.class)
                .where("TREE_ID").eq(999)
                .read();
        Assertions.assertTrue(EntityComparator.getInstance(Tree.class).equals(expected, result));
    }
}
