package io.github.ulisse1996.jaorm.dsl.query.impl.simple;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.enums.JoinType;
import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.dsl.query.impl.AbstractLimitOffsetImpl;
import io.github.ulisse1996.jaorm.dsl.query.simple.FromSimpleSelected;
import io.github.ulisse1996.jaorm.dsl.query.simple.SimpleSelected;
import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.*;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
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
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleSelectedImpl extends AbstractLimitOffsetImpl implements SimpleSelected, FromSimpleSelected,
        SimpleOrder, SimpleSelectedWhere, SimpleSelectedLimit, SimpleSelectedOffset {

    private static final String SPACE = " ";
    private final List<AliasColumn> columns;
    private final List<SimpleJoinImpl> joins;
    private final List<SimpleSelectedImpl> unions;
    private final List<OrderImpl> orders;
    private final List<SimpleWhereImpl<?>> wheres;
    private SimpleWhereImpl<?> lastWhere;
    private String table;
    private String alias;
    private QueryConfig configuration = QueryConfig.builder().build();
    private int limit;
    private int offset;

    public SimpleSelectedImpl(List<AliasColumn> columns) {
        this.columns = Collections.unmodifiableList(
                Checker.assertNotNull(columns, "columns")
        );
        this.joins = new ArrayList<>();
        this.unions = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.wheres = new ArrayList<>();
    }

    @Override
    public FromSimpleSelected from(String table) {
        this.table = Checker.assertNotNull(table, "table");
        return this;
    }

    @Override
    public <R> R read(Class<R> klass) {
        checkProjection(klass);
        Pair<String, List<SqlParameter>> build = doBuild();
        return QueryRunner.getInstance(klass).read(klass, build.getKey(), build.getValue());
    }

    @Override
    public <R> Optional<R> readOpt(Class<R> klass) {
        checkProjection(klass);
        Pair<String, List<SqlParameter>> build = doBuild();
        return QueryRunner.getInstance(klass).readOpt(klass, build.getKey(), build.getValue())
                .toOptional();
    }

    @Override
    public <R> List<R> readAll(Class<R> klass) {
        checkProjection(klass);
        Pair<String, List<SqlParameter>> build = doBuild();
        return QueryRunner.getInstance(klass).readAll(klass, build.getKey(), build.getValue());
    }

    @Override
    public WithProjectionResult union(WithProjectionResult union) {
        SimpleSelectedImpl selected;
        if (union instanceof SimpleJoinImpl) {
            selected = ((SimpleJoinImpl) union).getParent();
        } else {
            selected = (SimpleSelectedImpl) union;
        }
        this.unions.add(selected);
        return this;
    }

    private void checkProjection(Class<?> klass) {
        ProjectionsService.getInstance().searchDelegate(klass);
    }

    public FromSimpleSelected from(String table, String alias) {
        this.table = table;
        this.alias = alias;
        return this;
    }

    private Pair<String, List<SqlParameter>> doBuild() {
        List<SqlParameter> parameters = new ArrayList<>();
        AliasesSpecific aliasesSpecific = VendorSpecific.getSpecific(AliasesSpecific.class);
        StringBuilder builder = new StringBuilder("SELECT ")
                .append(asSelectColumns(aliasesSpecific, parameters))
                .append("FROM ")
                .append(this.alias != null ? String.format("%s%s", this.table, aliasesSpecific.convertToAlias(this.alias)) : this.table)
                .append(this.joins.isEmpty() ? "" : SPACE);
        for (SimpleJoinImpl join : this.joins) {
            Pair<String, List<SqlParameter>> build = join.doBuild();
            parameters.addAll(build.getValue());
            builder.append(build.getKey()).append(SPACE);
        }
        String where = buildWheres(getConfiguration().isCaseInsensitive());
        builder.append(this.joins.isEmpty() ? where : where.trim());
        parameters.addAll(
                this.wheres.stream()
                        .flatMap(m -> m.getParameters(getConfiguration().isCaseInsensitive()))
                        .collect(Collectors.toList())
        );
        for (OrderImpl order : this.orders) {
            builder.append(this.joins.isEmpty() ? SPACE : "")
                    .append("ORDER BY ")
                    .append(order.asString());
        }
        buildLimitOffset(builder, limit, offset);
        for (SimpleSelectedImpl union : this.unions) {
            Pair<String, List<SqlParameter>> build = union.doBuild();
            parameters.addAll(build.getValue());
            builder.append(this.joins.isEmpty() ? SPACE : "")
                    .append("UNION ").append(build.getKey());
        }
        return new Pair<>(builder.toString(), parameters);
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
                }).collect(Collectors.joining(", ")) + " ";
        parameters.addAll(
                this.columns.stream()
                        .filter(el -> el.getFunction() != null && el.getFunction().supportParams())
                        .map(AliasColumn::getFunction)
                        .flatMap(el -> ((VendorFunctionWithParams<?>) el).getParams().stream())
                        .map(SqlParameter::new)
                        .collect(Collectors.toList())
        );
        return s;
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
