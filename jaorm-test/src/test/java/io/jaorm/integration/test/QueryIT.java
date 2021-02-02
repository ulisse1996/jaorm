package io.jaorm.integration.test;

import io.jaorm.entity.EntityComparator;
import io.jaorm.entity.EntityDelegate;
import io.jaorm.entity.QueriesService;
import io.jaorm.integration.test.entity.Tree;
import io.jaorm.integration.test.query.TreeDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class QueryIT extends AbstractIT {

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_create_new_entity(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        TreeDAO dao = QueriesService.getInstance().getQuery(TreeDAO.class);

        Tree tree = new Tree();
        tree.setId(1);

        Tree inserted = dao.insert(tree);
        Assertions.assertTrue(inserted instanceof EntityDelegate);

        Tree found = dao.read(tree);
        Assertions.assertNotSame(tree, found);
        Assertions.assertEquals(tree.getId(), found.getId());
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_update_all_entity(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Tree tree = getTree(1);
        Tree tree2 = getTree(2);
        Tree tree3 = getTree(3);
        Tree tree4 = getTree(4);
        Tree tree5 = getTree(5);

        TreeDAO dao = QueriesService.getInstance().getQuery(TreeDAO.class);

        dao.insert(Arrays.asList(tree, tree2, tree3, tree4, tree5));

        List<Tree> trees = dao.readAll();

        Assertions.assertEquals(5, trees.size());
        trees.sort(Comparator.comparing(Tree::getId));

        Assertions.assertEquals(trees.get(0), tree);
        Assertions.assertEquals(trees.get(1), tree2);
        Assertions.assertEquals(trees.get(2), tree3);
        Assertions.assertEquals(trees.get(3), tree4);
        Assertions.assertEquals(trees.get(4), tree5);

        List<Tree> modTrees = trees.stream()
                .peek(t -> t.setFruitId(t.getId() + 10))
                .collect(Collectors.toList());

        dao.update(modTrees);

        trees = dao.readAll();
        Assertions.assertEquals(modTrees, trees);
    }

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_delete_entity(HSQLDBProvider.DatabaseType type, String initSql) {
        setDataSource(type, initSql);

        Tree tree = new Tree();
        tree.setId(1);
        tree.setName("NAME");
        tree.setFruitId(1);

        TreeDAO dao = QueriesService.getInstance().getQuery(TreeDAO.class);

        tree = dao.insert(tree);
        Optional<Tree> optionalTree = dao.readOpt(tree);

        Assertions.assertTrue(optionalTree.isPresent());
        Assertions.assertTrue(EntityComparator.getInstance(Tree.class).equals(tree, optionalTree.get()));

        dao.delete(tree);

        optionalTree = dao.readOpt(tree);
        Assertions.assertFalse(optionalTree.isPresent());
    }

    private Tree getTree(int i) {
        Tree tree = new Tree();
        tree.setId(i);
        tree.setName("TREE_" + i);
        return tree;
    }
}
