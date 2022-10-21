package io.github.ulisse1996.jaorm.dsl.query.impl.simple;

import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.query.enums.JoinType;
import io.github.ulisse1996.jaorm.dsl.query.simple.FromSimpleSelected;
import io.github.ulisse1996.jaorm.dsl.query.simple.SimpleSelected;
import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.SimpleOn;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
import io.github.ulisse1996.jaorm.dsl.util.Checker;
import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.SqlColumnWithAlias;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.ProjectionsService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithAlias;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleSelectedImpl implements SimpleSelected, FromSimpleSelected {

    private static final String SPACE = " ";
    private final List<AliasColumn> columns;
    private final List<SimpleJoinImpl> joins;
    private final List<SimpleSelectedImpl> unions;
    private String table;
    private String alias;
    private QueryConfig configuration = QueryConfig.builder().build();

    public SimpleSelectedImpl(List<AliasColumn> columns) {
        this.columns = Collections.unmodifiableList(
                Checker.assertNotNull(columns, "columns")
        );
        this.joins = new ArrayList<>();
        this.unions = new ArrayList<>();
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
        SimpleSelectedImpl impl = (SimpleSelectedImpl) union;
        this.unions.add(impl);
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
        for (SimpleSelectedImpl union : this.unions) {
            Pair<String, List<SqlParameter>> build = union.doBuild();
            parameters.addAll(build.getValue());
            builder.append(" UNION ").append(build.getKey());
        }
        return new Pair<>(builder.toString(), parameters);
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
}
