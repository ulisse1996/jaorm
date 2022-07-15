package io.github.ulisse1996.jaorm.graph;

import io.github.ulisse1996.jaorm.ResultSetExecutor;
import io.github.ulisse1996.jaorm.entity.*;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.JaormSqlException;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityGraph<T> {

    private static final Pattern CAMEL_CASE_TO_SNAKE_CASE_PATTERN = Pattern
            .compile("((?<=[a-z0-9])[A-Z]|(?!^)[A-Z](?=[a-z]))");
    private final Class<T> klass;
    private final List<Node> nodes;

    public EntityGraph(Class<T> klass, List<Node> nodes) {
        this.klass = klass;
        this.nodes = nodes;
    }

    public static <T> Builder<T> builder(Class<T> klass) {
        return new Builder<>(klass);
    }

    public static class Builder<T> {

        private final Class<T> klass;
        private final List<Node> nodes;

        private Builder(Class<T> klass) {
            this.klass = klass;
            this.nodes = new ArrayList<>();
        }

        public void addChild(Class<?> klass, String joinClause, NodeType nodeType,
                                   ColumnSetter<Object, Object> setter,
                                   ColumnGetter<Object, Object> getter,
                                    String alias) {
            this.nodes.add(new Node(klass, joinClause, nodeType, setter, getter, alias));
        }

        public EntityGraph<T> build() {
            return new EntityGraph<>(klass, nodes);
        }
    }

    private static class Node {

        private final Class<?> klass;
        private final String joinClause;
        private final NodeType nodeType;
        private final ColumnSetter<Object, Object> setter;
        private final ColumnGetter<Object, Object> getter;
        private final String nodeName;

        public Node(Class<?> klass, String joinClause, NodeType nodeType,
                    ColumnSetter<Object, Object> setter,
                    ColumnGetter<Object, Object> getter,
                    String nodeName) {
            this.nodeName = nodeName;
            this.klass = klass;
            this.joinClause = joinClause;
            this.nodeType = nodeType;
            this.setter = setter;
            this.getter = getter;
        }
    }

    public T fetch(T entity) {
        return doFetch(entity, false);
    }

    public Optional<T> fetchOpt(T entity) {
        return Optional.ofNullable(doFetch(entity, true));
    }

    @SuppressWarnings("unchecked")
    private T doFetch(T entity, boolean opt) {
        Objects.requireNonNull(entity, "Entity can't be null");
        EntityDelegate<?> entityDelegate = DelegatesService.getInstance().searchDelegate(this.klass).get();
        String sql = buildSql(entityDelegate);
        List<SqlParameter> sqlParameters = ((EntityMapper<T>) entityDelegate.getEntityMapper()).getKeys(entity).asSqlParameters();
        QueryRunner.logger.logSql(sql, sqlParameters);
        try (Connection connection = QueryRunner.getInstance(entity.getClass())
                .getConnection(DelegatesService.getInstance().getTableInfo(entity.getClass()));
             PreparedStatement pr = connection.prepareStatement(sql);
             ResultSetExecutor rs = new ResultSetExecutor(pr, sqlParameters)) {
            boolean hasNext = rs.getResultSet().next();
            if (!hasNext && opt) {
                return null;
            }
            return mapResults(rs.getResultSet());
        } catch (SQLException ex) {
            throw new JaormSqlException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private T mapResults(ResultSet resultSet) throws SQLException {
        EntityDelegate<T> entityDelegate = (EntityDelegate<T>) DelegatesService.getInstance().searchDelegate(this.klass).get();
        EntityMapper<T> entityMapper = entityDelegate.getEntityMapper();
        T entity = entityMapper.mapForGraph(entityDelegate.getEntityInstance(), resultSet, createAlias(this.klass));
        List<Node> optOrSingleNodes = this.nodes.stream()
                .filter(n -> Arrays.asList(NodeType.SINGLE, NodeType.OPTIONAL).contains(n.nodeType))
                .collect(Collectors.toList());
        List<Node> collectionNodes = this.nodes.stream()
                .filter(n -> NodeType.COLLECTION.equals(n.nodeType))
                .collect(Collectors.toList());
        for (Node node : optOrSingleNodes) {
            EntityDelegate<Object> nodeDelegate = (EntityDelegate<Object>) DelegatesService.getInstance().searchDelegate(node.klass).get();
            EntityMapper<Object> nodeMapper = nodeDelegate.getEntityMapper();
            Object nodeEntity = nodeMapper.mapForGraph(nodeDelegate.getEntityInstance(), resultSet, node.nodeName);
            boolean notFound = !nodeMapper.containsGraphResult(resultSet, node.nodeName);
            if (NodeType.OPTIONAL.equals(node.nodeType)) {
                if (notFound) {
                    nodeEntity = Result.empty();
                } else {
                    nodeEntity = Result.of(nodeEntity);
                }
            } else if (notFound) {
                throw new JaormSqlException("Can't map single result for graph fetch");
            }
            node.setter.accept(entity, nodeEntity);
        }
        do {
            for (Node node : collectionNodes) {
                EntityDelegate<Object> nodeDelegate = (EntityDelegate<Object>) DelegatesService.getInstance().searchDelegate(node.klass).get();
                EntityMapper<Object> nodeMapper = nodeDelegate.getEntityMapper();
                List<Object> list = (List<Object>) setList(node, entity);
                list.add(nodeMapper.mapForGraph(nodeDelegate.getEntityInstance(), resultSet, node.nodeName));
            }
        } while (resultSet.next());
        return entity;
    }

    private List<?> setList(Node node, T entity) {
        List<?> list = (List<?>) node.getter.apply(entity);
        if (list == null) {
            node.setter.accept(entity, new ArrayList<>());
        }
        return list == null ? (List<?>) node.getter.apply(entity) : list;
    }

    private String buildSql(EntityDelegate<?> entityDelegate) {
        List<String> joins = new ArrayList<>();
        String table = createAlias(this.klass);
        String where = entityDelegate.getKeysWhere(table);
        StringBuilder builder = new StringBuilder("SELECT ");
        List<String> selected = Stream.of(entityDelegate.getSelectables())
                .map(s -> String.format("%s.%s AS \"%s.%s\"", table, s, table, s)).collect(Collectors.toList());
        for (Node node : nodes) {
            EntityDelegate<?> del = DelegatesService.getInstance().searchDelegate(node.klass).get();
            selected.addAll(
                    Stream.of(del.getSelectables())
                            .map(s -> String.format("%s.%s AS \"%s.%s\"", node.nodeName, s, node.nodeName, s))
                            .collect(Collectors.toList())
            );
            joins.add(String.format(" %s %s %s %s", joinType(node.nodeType), del.getTable(), node.nodeName, node.joinClause));
        }
        return builder.append(String.join(", ", selected)).append(String.format(" FROM %s %s", entityDelegate.getTable(), table))
                .append(String.join("", joins))
                .append(where)
                .toString();
    }

    private String createAlias(Class<T> klass) {
        return CAMEL_CASE_TO_SNAKE_CASE_PATTERN.matcher(klass.getSimpleName())
                .replaceAll("_$1").toLowerCase() + "_" + 0;
    }

    private String joinType(NodeType nodeType) {
        return NodeType.OPTIONAL.equals(nodeType) ? "LEFT JOIN" : "JOIN";
    }
}
