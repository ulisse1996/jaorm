package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.config.DefaultWhereChecker;
import io.github.ulisse1996.jaorm.dsl.config.QueryConfig;
import io.github.ulisse1996.jaorm.dsl.config.WhereChecker;
import io.github.ulisse1996.jaorm.dsl.query.common.*;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateJoin;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.On;
import io.github.ulisse1996.jaorm.dsl.query.enums.JoinType;
import io.github.ulisse1996.jaorm.dsl.query.enums.OrderType;
import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelectedImpl<T, N> implements Selected<T>, SelectedWhere<T>, SelectedOn<T, N>,
        SelectedLimit<T>, SelectedOffset<T>, SelectedOrder<T> {

    private final List<String> columns;
    protected final String table;
    private final Class<T> klass;
    private final List<WhereImpl<T, ?>> wheres;
    private final List<JoinImpl<T, ?, ?>> joins;
    private final List<OrderImpl> orders;
    private final boolean caseInsensitiveLike;
    private WhereChecker checker;
    private WhereImpl<T, ?> lastWhere;
    private JoinImpl<T, ?, ?> lastJoin;
    private int limit;
    private int offset;

    public SelectedImpl(Class<T> klass, QueryConfig config) {
        this(klass, config.isCaseInsensitive());
        this.checker = config.getChecker();
    }

    public SelectedImpl(Class<T> klass, boolean caseInsensitiveLike) {
        EntityDelegate<?> delegate = DelegatesService.getInstance()
                .searchDelegate(klass).get();
        this.klass = klass;
        this.columns = Arrays.asList(delegate.getSelectables());
        this.table = delegate.getTable();
        this.caseInsensitiveLike = caseInsensitiveLike;
        this.wheres = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.checker = new DefaultWhereChecker();
    }

    public WhereChecker getChecker() {
        return checker;
    }

    @Override
    public T read() {
        Pair<String, List<SqlParameter>> pair = doBuild(false);
        return QueryRunner.getInstance(this.klass)
                .read(klass, pair.getKey(), pair.getValue());
    }

    @Override
    public Optional<T> readOpt() {
        Pair<String, List<SqlParameter>> pair = doBuild(false);
        return QueryRunner.getInstance(this.klass)
                .readOpt(klass, pair.getKey(), pair.getValue()).toOptional();
    }

    @Override
    public List<T> readAll() {
        Pair<String, List<SqlParameter>> pair = doBuild(false);
        return QueryRunner.getInstance(this.klass)
                .readAll(klass, pair.getKey(), pair.getValue());
    }

    @Override
    public long count() {
        Pair<String, List<SqlParameter>> pair = doBuild(true);
        return QueryRunner.getSimple().read(long.class, pair.getKey(), pair.getValue());
    }

    public List<SqlParameter> getParameters() {
        return this.wheres.stream().flatMap(WhereImpl::getParameters)
                .collect(Collectors.toList());
    }

    @Override
    public <R> IntermediateWhere<T, R> where(SqlColumn<?, R> column) {
        return addAndReturnLast(column, false, null);
    }

    @Override
    public <R> IntermediateWhere<T, R> where(SqlColumn<?, R> column, String alias) {
        return addAndReturnLast(column, false, alias);
    }

    @Override
    public <R> IntermediateWhere<T, R> where(VendorFunction<R> column) {
        return addAndReturnLast(column, false, null);
    }

    @Override
    public <R> IntermediateWhere<T, R> where(VendorFunction<R> column, String alias) {
        return addAndReturnLast(column, false, alias);
    }

    @Override
    public <R> IntermediateWhere<T, R> andWhere(SqlColumn<?, R> column) {
        return where(column);
    }

    @Override
    public <R> IntermediateWhere<T, R> orWhere(SqlColumn<?, R> column) {
        return addAndReturnLast(column, true, null);
    }

    @Override
    public <R> IntermediateWhere<T, R> and(SqlColumn<?, R> column) {
        return addAndReturnLinked(column, false, null);
    }

    @Override
    public <R> IntermediateWhere<T, R> or(SqlColumn<?, R> column) {
        return addAndReturnLinked(column, true, null);
    }

    @Override
    public <R> IntermediateWhere<T, R> andWhere(SqlColumn<?, R> column, String alias) {
        return where(column, alias);
    }

    @Override
    public <R> IntermediateWhere<T, R> orWhere(SqlColumn<?, R> column, String alias) {
        return addAndReturnLast(column, true, alias);
    }

    @Override
    public <R> IntermediateWhere<T, R> and(SqlColumn<?, R> column, String alias) {
        return addAndReturnLinked(column, false, alias);
    }

    @Override
    public <R> IntermediateWhere<T, R> or(SqlColumn<?, R> column, String alias) {
        return addAndReturnLinked(column, true, alias);
    }

    @Override
    public <R> IntermediateWhere<T, R> andWhere(VendorFunction<R> function) {
        return where(function);
    }

    @Override
    public <R> IntermediateWhere<T, R> orWhere(VendorFunction<R> function) {
        return addAndReturnLast(function, true, null);
    }

    @Override
    public <R> IntermediateWhere<T, R> and(VendorFunction<R> function) {
        return addAndReturnLinked(function, false, null);
    }

    @Override
    public <R> IntermediateWhere<T, R> or(VendorFunction<R> function) {
        return addAndReturnLinked(function, true, null);
    }

    @Override
    public <R> IntermediateWhere<T, R> andWhere(VendorFunction<R> function, String alias) {
        return where(function, alias);
    }

    @Override
    public <R> IntermediateWhere<T, R> orWhere(VendorFunction<R> function, String alias) {
        return addAndReturnLast(function, true, alias);
    }

    @Override
    public <R> IntermediateWhere<T, R> and(VendorFunction<R> function, String alias) {
        return addAndReturnLinked(function, false, alias);
    }

    @Override
    public <R> IntermediateWhere<T, R> or(VendorFunction<R> function, String alias) {
        return addAndReturnLinked(function, true, alias);
    }

    @Override
    public <R> On<T, R> join(Class<R> table) {
        return addJoin(table, JoinType.JOIN, null);
    }

    @Override
    public <R> On<T, R> leftJoin(Class<R> table) {
        return addJoin(table, JoinType.LEFT_JOIN, null);
    }

    @Override
    public <R> On<T, R> rightJoin(Class<R> table) {
        return addJoin(table, JoinType.RIGHT_JOIN, null);
    }

    @Override
    public <R> On<T, R> fullJoin(Class<R> table) {
        return addJoin(table, JoinType.FULL_JOIN, null);
    }

    @Override
    public <R> On<T, R> join(Class<R> table, String alias) {
        return addJoin(table, JoinType.JOIN, alias);
    }

    @Override
    public <R> On<T, R> leftJoin(Class<R> table, String alias) {
        return addJoin(table, JoinType.LEFT_JOIN, alias);
    }

    @Override
    public <R> On<T, R> rightJoin(Class<R> table, String alias) {
        return addJoin(table, JoinType.RIGHT_JOIN, alias);
    }

    @Override
    public <R> On<T, R> fullJoin(Class<R> table, String alias) {
        return addJoin(table, JoinType.FULL_JOIN, alias);
    }

    @Override
    public <L> IntermediateJoin<T, N, L> orOn(SqlColumn<N, L> column) {
        return this.lastJoin.orOn(column);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <L> IntermediateJoin<T, N, L> on(SqlColumn<N, L> column) {
        JoinImpl<T, N, ?> join = (JoinImpl<T, N, ?>) this.lastJoin;
        return join.on(column);
    }

    @Override
    public SelectedLimit<T> limit(int rows) {
        if (rows <= 0) {
            throw new IllegalArgumentException("Limit can't be <= 0");
        }
        this.limit = rows;
        return this;
    }

    @Override
    public SelectedOffset<T> offset(int rows) {
        if (rows <= 0) {
            throw new IllegalArgumentException("Offset can't be <= 0");
        }
        this.offset = rows;
        return this;
    }

    @Override
    public SelectedOrder<T> orderBy(OrderType type, SqlColumn<?, ?> column, String alias) {
        this.orders.add(new OrderImpl(type, column, alias, this));
        return this;
    }

    @Override
    public SelectedOrder<T> orderBy(OrderType type, SqlColumn<?, ?> column) {
        this.orders.add(new OrderImpl(type, column, null, this));
        return this;
    }

    @SuppressWarnings("unchecked")
    private <R> On<T, R> addJoin(Class<R> table, JoinType joinType, String alias) {
        JoinImpl<T, R, ?> join = new JoinImpl<>(table, this, joinType, alias);
        this.joins.add(join);
        this.lastJoin = join;
        return (On<T, R>) this.lastJoin;
    }

    private <R> IntermediateWhere<T,R> addAndReturnLinked(VendorFunction<R> function, boolean or, String alias) {
        WhereImpl<T, R> where = new SelectedWhereFunctionImpl<>(function, this, or, alias);
        this.lastWhere.addLinked(where);
        return where;
    }

    @SuppressWarnings("unchecked")
    private <R> IntermediateWhere<T, R> addAndReturnLast(VendorFunction<R> function, boolean or, String alias) {
        WhereImpl<T, R> where = new SelectedWhereFunctionImpl<>(function, this, or, alias);
        this.wheres.add(where);
        this.lastWhere = where;
        return (IntermediateWhere<T, R>) this.lastWhere;
    }

    private <R> IntermediateWhere<T,R> addAndReturnLinked(SqlColumn<?, R> column, boolean or, String alias) {
        WhereImpl<T, R> where = new WhereImpl<>(column, this, or, alias);
        this.lastWhere.addLinked(where);
        return where;
    }

    @SuppressWarnings("unchecked")
    private <R> IntermediateWhere<T, R> addAndReturnLast(SqlColumn<?, R> column, boolean or, String alias) {
        WhereImpl<T, R> where = new WhereImpl<>(column, this, or, alias);
        this.wheres.add(where);
        this.lastWhere = where;
        return (IntermediateWhere<T, R>) this.lastWhere;
    }

    private Pair<String, List<SqlParameter>> doBuild(boolean count) {
        return new Pair<>(this.asString(count), this.getParameters());
    }

    public String asString(boolean count) {
        StringBuilder builder = new StringBuilder("SELECT ")
                .append(count ? "COUNT(*) " : asSelectColumns())
                .append("FROM ")
                .append(this.table);
        return buildExtraSql(builder);
    }

    protected String buildExtraSql(StringBuilder builder) {
        for (JoinImpl<T, ?, ?> j : this.joins) {
            builder.append(j.asString(caseInsensitiveLike));
        }
        boolean first = true;
        for (WhereImpl<?, ?> where : wheres) {
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
        if (!this.orders.isEmpty()) {
            buildOrder(builder);
        }
        if (offset > 0 && limit > 0) {
            builder.append(VendorSpecific.getSpecific(LimitOffsetSpecific.class).convertOffSetLimitSupport(limit, offset));
        } else if (limit > 0) {
            builder.append(VendorSpecific.getSpecific(LimitOffsetSpecific.class).convertOffSetLimitSupport(limit));
        } else if (offset > 0) {
            builder.append(VendorSpecific.getSpecific(LimitOffsetSpecific.class).convertOffsetSupport(offset));
        }
        return builder.toString();
    }

    private void buildOrder(StringBuilder builder) {
        builder.append(" ORDER BY");
        for (int i = 0; i < this.orders.size(); i++) {
            builder.append(this.orders.get(i).asString());
            if (this.orders.size() - 1 != i) {
                builder.append(",");
            }
        }
    }

    private String asSelectColumns() {
        return this.columns.stream()
                .map(s -> String.format("%s.%s", this.table, s))
                .collect(Collectors.joining(", ")) + " ";
    }

    public String getTable() {
        return this.table;
    }
}
