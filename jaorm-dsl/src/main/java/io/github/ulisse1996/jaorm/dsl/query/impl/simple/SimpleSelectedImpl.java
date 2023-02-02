package io.github.ulisse1996.jaorm.dsl.query.impl.simple;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.enums.JoinType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.dsl.query.impl.AbstractLimitOffsetImpl;
import io.github.ulisse1996.jaorm.dsl.query.simple.FromSimpleSelected;
import io.github.ulisse1996.jaorm.dsl.query.simple.SimpleSelected;
import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.*;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithResult;
import io.github.ulisse1996.jaorm.dsl.util.Checker;
import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.SqlColumnWithAlias;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithAlias;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.ansi.AggregateFunction;
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleSelectedImpl extends AbstractLimitOffsetImpl implements SimpleSelected, FromSimpleSelected,
        SimpleOrder, SimpleSelectedWhere, SimpleSelectedLimit, SimpleSelectedOffset, SimpleGroup, SimpleHaving {

    private static final String SPACE = " ";
    private final List<AliasColumn> columns;
    private final List<SimpleJoinImpl> joins;
    private final List<SimpleSelectedImpl> unions;
    private final List<OrderImpl> orders;
    private final List<SimpleWhereImpl<?>> wheres;
    private final List<GroupImpl> groups;
    private final List<IntermediateSimpleHavingImpl<?>> havings;
    private final boolean distinct;
    private SimpleWhereImpl<?> lastWhere;
    private String table;
    private String alias;
    private QueryConfig configuration = QueryConfig.builder().build();
    private int limit;
    private int offset;

    public SimpleSelectedImpl(List<AliasColumn> columns, boolean distinct) {
        this.distinct = distinct;
        this.columns = Collections.unmodifiableList(
                Checker.assertNotNull(columns, "columns")
        );
        this.joins = new ArrayList<>();
        this.unions = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.wheres = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.havings = new ArrayList<>();
    }

    @Override
    public FromSimpleSelected from(String table) {
        this.table = Checker.assertNotNull(table, "table");
        return this;
    }

    @Override
    public <R> R read(Class<R> klass) {
        checkProjectionOrSimpleType(klass);
        Pair<String, List<SqlParameter>> build = doBuild();
        return QueryRunner.getInstance(klass).read(klass, build.getKey(), build.getValue());
    }

    @Override
    public <R> Optional<R> readOpt(Class<R> klass) {
        checkProjectionOrSimpleType(klass);
        Pair<String, List<SqlParameter>> build = doBuild();
        return QueryRunner.getInstance(klass).readOpt(klass, build.getKey(), build.getValue())
                .toOptional();
    }

    @Override
    public <R> List<R> readAll(Class<R> klass) {
        checkProjectionOrSimpleType(klass);
        Pair<String, List<SqlParameter>> build = doBuild();
        return QueryRunner.getInstance(klass).readAll(klass, build.getKey(), build.getValue());
    }

    @Override
    public WithResult union(WithResult union) {
        SimpleSelectedImpl selected;
        if (union instanceof SimpleJoinImpl) {
            selected = ((SimpleJoinImpl) union).getParent();
        } else {
            selected = (SimpleSelectedImpl) union;
        }
        this.unions.add(selected);
        return this;
    }

    private void checkProjectionOrSimpleType(Class<?> klass) {
        try {
            ProjectionsService.getInstance().searchDelegate(klass);
        } catch (IllegalArgumentException ex) {
            // Skip for simple read with only one column
            if (this.columns.size() != 1) {
                throw ex;
            }
        }
    }

    public FromSimpleSelected from(String table, String alias) {
        this.table = table;
        this.alias = alias;
        return this;
    }

    private Pair<String, List<SqlParameter>> doBuild() {
        List<SqlParameter> parameters = new ArrayList<>();
        AliasesSpecific aliasesSpecific = VendorSpecific.getSpecific(AliasesSpecific.class);

        List<String> parts = new ArrayList<>();
        parts.add(asSelectColumns(aliasesSpecific, parameters));
        parts.add(String.format("FROM %s", this.alias != null ? String.format("%s%s", this.table, aliasesSpecific.convertToAlias(this.alias)) : this.table));
        for (SimpleJoinImpl join : this.joins) {
            Pair<String, List<SqlParameter>> build = join.doBuild();
            parameters.addAll(build.getValue());
            parts.add(build.getKey());
        }
        parts.add(buildWheres(getConfiguration().isCaseInsensitive()).trim());
        parameters.addAll(
                this.wheres.stream()
                        .flatMap(m -> m.getParameters(getConfiguration().isCaseInsensitive()))
                        .collect(Collectors.toList())
        );
        parts.add(buildGroups());
        parts.add(buildHaving(parameters));
        parts.add(buildOrder());
        StringBuilder builder = new StringBuilder();
        buildLimitOffset(builder, limit, offset);
        parts.add(builder.toString().trim());
        parts.add(buildUnion(parameters));

        String result = parts.stream()
                .filter(el -> el != null && !el.isEmpty())
                .collect(Collectors.joining(SPACE));

        return new Pair<>(result, parameters);
    }

    private String buildUnion(List<SqlParameter> parameters) {
        StringBuilder builder = new StringBuilder();
        for (SimpleSelectedImpl union : this.unions) {
            Pair<String, List<SqlParameter>> build = union.doBuild();
            parameters.addAll(build.getValue());
            builder.append("UNION ").append(build.getKey());
        }
        return builder.toString();
    }

    private String buildOrder() {
        if (this.orders.isEmpty()) {
            return null;
        }
        return "ORDER BY " + this.orders.stream()
                .map(OrderImpl::asString)
                .collect(Collectors.joining(","));
    }

    private String buildHaving(List<SqlParameter> parameters) {
        if (this.havings.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder("HAVING ");
        for (int i = 0; i < this.havings.size(); i++) {
            IntermediateSimpleHavingImpl<?> having = this.havings.get(i);
            parameters.add(new SqlParameter(having.val));
            if (i != 0) {
                builder.append(SPACE);
            }
            builder.append(having.asString(i == 0));
        }
        return builder.toString();
    }

    private String buildGroups() {
        if (this.groups.isEmpty()) {
            return null;
        }

        return "GROUP BY " + this.groups.stream()
                .map(GroupImpl::asString)
                .collect(Collectors.joining(","));
    }

    private String buildWheres(boolean caseInsensitiveLike) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (SimpleWhereImpl<?> where : wheres) {
            if (first) {
                String gen = where.asString(true, caseInsensitiveLike);
                builder.append(gen);
                if (!gen.trim().isEmpty()) {
                    first = false;
                }
            } else {
                builder.append(where.asString(false, caseInsensitiveLike));
            }
        }
        return builder.toString();
    }

    private String asSelectColumns(AliasesSpecific aliasesSpecific, List<SqlParameter> parameters) {
        String s = this.columns.stream()
                .map(i -> {
                    if (i.getColumn() != null) {
                        return getSelectColumn(i, aliasesSpecific);
                    } else {
                        return getVendorFunctionColumn(i, aliasesSpecific);
                    }
                }).collect(Collectors.joining(", "));
        parameters.addAll(
                this.columns.stream()
                        .filter(el -> el.getFunction() != null && el.getFunction().supportParams())
                        .map(AliasColumn::getFunction)
                        .flatMap(el -> ((VendorFunctionWithParams<?>) el).getParams().stream())
                        .map(SqlParameter::new)
                        .collect(Collectors.toList())
        );
        return "SELECT " + (distinct ? "DISTINCT " : "") + s;
    }

    private String getVendorFunctionColumn(AliasColumn i, AliasesSpecific aliasesSpecific) {
        String columnAlias = "";
        if (i.getFunction() instanceof VendorFunctionWithAlias) {
            columnAlias = aliasesSpecific.convertToAlias(((VendorFunctionWithAlias<?>) i.getFunction()).getAlias());
        }
        return String.format(
                "%s%s",
                i.getFunction().apply(i.getTableAlias()),
                columnAlias
        );
    }

    private static String getSelectColumn(AliasColumn i, AliasesSpecific aliasesSpecific) {
        String columnAlias = "";
        if (i.getColumn() instanceof SqlColumnWithAlias) {
            columnAlias = aliasesSpecific.convertToAlias(((SqlColumnWithAlias<?, ?>) i.getColumn()).getAlias());
        }
        if (i.getTableAlias() != null) {
            return String.format(
                    "%s.%s%s",
                    i.getTableAlias(),
                    i.getColumn().getName(),
                    columnAlias
            );
        } else {
            return String.format(
                    "%s%s",
                    i.getColumn().getName(),
                    columnAlias
            );
        }
    }

    @Override
    public SimpleOn join(String table) {
        return join(table, null);
    }

    @Override
    public SimpleOn leftJoin(String table) {
        return leftJoin(table, null);
    }

    @Override
    public SimpleOn rightJoin(String table) {
        return rightJoin(table, null);
    }

    @Override
    public SimpleOn fullJoin(String table) {
        return fullJoin(table, null);
    }

    @Override
    public SimpleOn join(String table, String alias) {
        this.joins.add(new SimpleJoinImpl(table, alias, this, JoinType.JOIN));
        return this.joins.get(this.joins.size() - 1);
    }

    @Override
    public SimpleOn leftJoin(String table, String alias) {
        this.joins.add(new SimpleJoinImpl(table, alias, this, JoinType.LEFT_JOIN));
        return this.joins.get(this.joins.size() - 1);
    }

    @Override
    public SimpleOn rightJoin(String table, String alias) {
        this.joins.add(new SimpleJoinImpl(table, alias, this, JoinType.RIGHT_JOIN));
        return this.joins.get(this.joins.size() - 1);
    }

    @Override
    public SimpleOn fullJoin(String table, String alias) {
        this.joins.add(new SimpleJoinImpl(table, alias, this, JoinType.FULL_JOIN));
        return this.joins.get(this.joins.size() - 1);
    }

    @Override
    public SimpleSelected withConfiguration(QueryConfig config) {
        this.configuration = config;
        return this;
    }

    QueryConfig getConfiguration() {
        return this.configuration;
    }

    void setConfiguration(QueryConfig config) {
        this.configuration = config;
    }

    @Override
    public SimpleOrder orderBy(OrderType type, SqlColumn<?, ?> column, String alias) {
        this.orders.add(new OrderImpl(type, alias, column));
        return this;
    }

    @Override
    public SimpleOrder orderBy(OrderType type, SqlColumn<?, ?> column) {
        return orderBy(type, column, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> where(SqlColumn<?, R> column) {
        return where(column, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> where(SqlColumn<?, R> column, String alias) {
        return addAndReturnLast(column, false, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> where(VendorFunction<R> column) {
        return where(column, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> where(VendorFunction<R> column, String alias) {
        return addAndReturnLast(column, false, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> andWhere(SqlColumn<?, R> column) {
        return where(column);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> orWhere(SqlColumn<?, R> column) {
        return addAndReturnLast(column, true, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> and(SqlColumn<?, R> column) {
        return addAndReturnLinked(column, false, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> or(SqlColumn<?, R> column) {
        return addAndReturnLinked(column, true, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> andWhere(SqlColumn<?, R> column, String alias) {
        return where(column, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> orWhere(SqlColumn<?, R> column, String alias) {
        return addAndReturnLast(column, true, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> and(SqlColumn<?, R> column, String alias) {
        return addAndReturnLinked(column, false, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> or(SqlColumn<?, R> column, String alias) {
        return addAndReturnLinked(column, true, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> andWhere(VendorFunction<R> function) {
        return where(function);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> orWhere(VendorFunction<R> function) {
        return addAndReturnLast(function, true, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> and(VendorFunction<R> function) {
        return addAndReturnLinked(function, false, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> or(VendorFunction<R> function) {
        return addAndReturnLinked(function, true, null);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> andWhere(VendorFunction<R> function, String alias) {
        return where(function, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> orWhere(VendorFunction<R> function, String alias) {
        return addAndReturnLast(function, true, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> and(VendorFunction<R> function, String alias) {
        return addAndReturnLinked(function, false, alias);
    }

    @Override
    public <R> IntermediateSimpleWhere<R> or(VendorFunction<R> function, String alias) {
        return addAndReturnLinked(function, true, alias);
    }

    private <R> IntermediateSimpleWhere<R> addAndReturnLinked(VendorFunction<R> function, boolean or, String alias) {
        SimpleWhereImpl<R> where = new SimpleSelectedWhereFunctionImpl<>(function, this, or, alias);
        this.lastWhere.addLinked(where);
        return where;
    }

    @SuppressWarnings("unchecked")
    private <R> IntermediateSimpleWhere<R> addAndReturnLast(VendorFunction<R> function, boolean or, String alias) {
        SimpleWhereImpl<R> where = new SimpleSelectedWhereFunctionImpl<>(function, this, or, alias);
        this.wheres.add(where);
        this.lastWhere = where;
        return (IntermediateSimpleWhere<R>) this.lastWhere;
    }

    private <R> IntermediateSimpleWhere<R> addAndReturnLinked(SqlColumn<?, R> column, boolean or, String alias) {
        SimpleWhereImpl<R> where = new SimpleWhereImpl<>(column, this, or, alias);
        this.lastWhere.addLinked(where);
        return where;
    }

    @SuppressWarnings("unchecked")
    private <R> IntermediateSimpleWhere<R> addAndReturnLast(SqlColumn<?, R> column, boolean or, String alias) {
        SimpleWhereImpl<R> where = new SimpleWhereImpl<>(column, this, or, alias);
        this.wheres.add(where);
        this.lastWhere = where;
        return (IntermediateSimpleWhere<R>) this.lastWhere;
    }

    @Override
    public String getSql() {
        return doBuild().getKey();
    }

    @Override
    public List<SqlParameter> getParameters() {
        return doBuild().getValue();
    }

    @Override
    public SimpleSelectedLimit limit(int rows) {
        if (rows <= 0) {
            throw new IllegalArgumentException("Limit can't be <= 0");
        }
        this.limit = rows;
        return this;
    }

    @Override
    public SimpleSelectedOffset offset(int rows) {
        if (rows <= 0) {
            throw new IllegalArgumentException("Offset can't be <= 0");
        }
        this.offset = rows;
        return this;
    }

    @Override
    public SimpleGroup groupBy(SqlColumn<?, ?> column) {
        return this.groupBy(column, null);
    }

    @Override
    public SimpleGroup groupBy(SqlColumn<?, ?> column, String alias) {
        this.groups.add(new GroupImpl(alias, column));
        return this;
    }

    @Override
    public SimpleGroup groupBy(SqlColumn<?, ?>... columns) {
        return this.groupBy(null, columns);
    }

    @Override
    public SimpleGroup groupBy(String alias, SqlColumn<?, ?>... columns) {
        if (columns.length == 0) {
            throw new IllegalArgumentException("groupBy require at least 1 column");
        }
        for (SqlColumn<?, ?> column : columns) {
            this.groups.add(new GroupImpl(alias, column));
        }
        return this;
    }

    @Override
    public <T> IntermediateSimpleHaving<T> having(AggregateFunction<T> function) {
        return having(function, null);
    }

    @Override
    public <T> IntermediateSimpleHaving<T> having(AggregateFunction<T> function, String alias) {
        IntermediateSimpleHavingImpl<T> having = new IntermediateSimpleHavingImpl<>(this, function, alias);
        this.havings.add(having);
        return having;
    }

    @Override
    public <T> IntermediateSimpleHaving<T> andHaving(AggregateFunction<T> function) {
        return this.having(function);
    }

    @Override
    public <T> IntermediateSimpleHaving<T> andHaving(AggregateFunction<T> function, String alias) {
        return this.having(function, alias);
    }

    @Override
    public <T> IntermediateSimpleHaving<T> orHaving(AggregateFunction<T> function) {
        return this.orHaving(function, null);
    }

    @Override
    public <T> IntermediateSimpleHaving<T> orHaving(AggregateFunction<T> function, String alias) {
        IntermediateSimpleHavingImpl<T> having = new IntermediateSimpleHavingImpl<>(this, function, alias, true);
        this.havings.add(having);
        return having;
    }

    private static class IntermediateSimpleHavingImpl<T> implements IntermediateSimpleHaving<T> {

        private final AggregateFunction<T> fn;
        private final SimpleSelectedImpl parent;
        private final String alias;
        private final boolean or;
        private T val;
        private Operation operation;

        public IntermediateSimpleHavingImpl(SimpleSelectedImpl parent, AggregateFunction<T> fn, String alias) {
            this(parent, fn, alias, false);
        }

        public IntermediateSimpleHavingImpl(SimpleSelectedImpl parent, AggregateFunction<T> fn, String alias, boolean or) {
            this.parent = parent;
            this.fn = fn;
            this.alias = alias;
            this.or = or;
        }

        public T getVal() {
            return val;
        }

        @Override
        public SimpleHaving eq(T val) {
            return operation(Operation.EQUALS, val);
        }

        @Override
        public SimpleHaving ne(T val) {
            return operation(Operation.NOT_EQUALS, val);
        }

        @Override
        public SimpleHaving lt(T val) {
            return operation(Operation.LESS_THAN, val);
        }

        @Override
        public SimpleHaving gt(T val) {
            return operation(Operation.GREATER_THAN, val);
        }

        @Override
        public SimpleHaving le(T val) {
            return operation(Operation.LESS_EQUALS, val);
        }

        @Override
        public SimpleHaving ge(T val) {
            return operation(Operation.GREATER_EQUALS, val);
        }

        @Override
        public SimpleHaving equalsTo(T val) {
            return eq(val);
        }

        @Override
        public SimpleHaving notEqualsTo(T val) {
            return ne(val);
        }

        @Override
        public SimpleHaving lessThan(T val) {
            return lt(val);
        }

        @Override
        public SimpleHaving greaterThan(T val) {
            return gt(val);
        }

        @Override
        public SimpleHaving lessOrEqualsTo(T val) {
            return le(val);
        }

        @Override
        public SimpleHaving greaterOrEqualsTo(T val) {
            return ge(val);
        }

        private SimpleHaving operation(Operation operation, T val) {
            this.operation = operation;
            this.val = val;
            return this.parent;
        }

        private String asString(boolean first) {
            String s;
            if (this.alias != null) {
                s = fn.apply(alias);
            } else {
                s = fn.apply(null);
            }
            if (first) {
                return String.format("%s%s?", s, operation.getValue());
            }
            return String.format("%s %s%s?", this.or ? "OR" : "AND", s, operation.getValue());
        }
    }

    private static class GroupImpl {
        private final String alias;
        private final SqlColumn<?, ?> column;

        private GroupImpl(String alias, SqlColumn<?, ?> column) {
            this.alias = alias;
            this.column = column;
        }

        private String asString() {
            if (this.alias != null) {
                return String.format("%s.%s", this.alias, this.column.getName());
            } else {
                return String.format("%s", this.column.getName());
            }
        }
    }

    private static class OrderImpl {

        private final OrderType type;
        private final String alias;
        private final SqlColumn<?, ?> column;

        private OrderImpl(OrderType type, String alias, SqlColumn<?, ?> column) {
            this.type = type;
            this.alias = alias;
            this.column = column;
        }

        private String asString() {
            if (this.alias != null) {
                return String.format("%s.%s %s", this.alias, this.column.getName(), type.name());
            } else {
                return String.format("%s %s", this.column.getName(), type.name());
            }
        }
    }
}
