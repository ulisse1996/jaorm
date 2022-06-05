package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.query.common.Case;
import io.github.ulisse1996.jaorm.dsl.query.common.Selected;
import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.CaseElse;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.CaseEnd;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.CaseThen;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.CaseWhen;
import io.github.ulisse1996.jaorm.dsl.query.common.trait.WithSubQuerySupport;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.dsl.util.Checker;
import io.github.ulisse1996.jaorm.dsl.util.Pair;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CaseImpl<L, R> implements Case<R>, CaseElse<R>, CaseEnd<R>, CaseThen<R>, CaseWhen<L, R> {

    private static final String SUB_QUERY = "subQuery";
    private static final String VALUE = "value";
    private static final String COLUMN = "column";
    private final LinkedList<WhenImpl> whens;
    private WhenImpl lastWhen;

    public CaseImpl() {
        this.whens = new LinkedList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M> CaseWhen<M, R> when(SqlColumn<?, M> column) {
        Checker.assertNotNull(column, COLUMN);
        this.lastWhen = new WhenImpl(column, false);
        this.whens.add(lastWhen);
        return (CaseWhen<M, R>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M> CaseWhen<M, R> when(SqlColumn<?, M> column, String alias) {
        Checker.assertNotNull(column, COLUMN);
        this.lastWhen = new WhenImpl(column, alias,false);
        this.whens.add(lastWhen);
        return (CaseWhen<M, R>) this;
    }

    @Override
    public CaseEnd<R> orElse(R value) {
        Checker.assertNotNull(value, VALUE);
        WhenImpl impl = new WhenImpl(null, null, true);
        impl.setValue(value);
        this.whens.add(impl);
        return this;
    }

    @Override
    public CaseEnd<R> orElse(SqlColumn<?, R> column) {
        Checker.assertNotNull(column, COLUMN);
        WhenImpl impl = new WhenImpl(column, null, true);
        this.whens.add(impl);
        return this;
    }

    @Override
    public CaseEnd<R> orElse(SqlColumn<?, R> column, String alias) {
        Checker.assertNotNull(column, COLUMN);
        WhenImpl impl = new WhenImpl(column, alias, true);
        this.whens.add(impl);
        return this;
    }

    @Override
    public CaseElse<R> then(R value) {
        Checker.assertNotNull(value, VALUE);
        this.lastWhen.setThen(value);
        return this;
    }

    @Override
    public CaseElse<R> then(SqlColumn<?, R> column) {
        Checker.assertNotNull(column, COLUMN);
        this.lastWhen.setThen(column);
        return this;
    }

    @Override
    public CaseElse<R> then(SqlColumn<?, R> column, String alias) {
        Checker.assertNotNull(column, COLUMN);
        this.lastWhen.setThen(column, alias);
        return this;
    }

    @Override
    public CaseThen<R> eq(L val) {
        Checker.assertNotNull(val, VALUE);
        setOperation(Operation.EQUALS, val, null);
        return this;
    }

    @Override
    public CaseThen<R> ne(L val) {
        Checker.assertNotNull(val, VALUE);
        setOperation(Operation.NOT_EQUALS, val, null);
        return this;
    }

    @Override
    public CaseThen<R> lt(L val) {
        Checker.assertNotNull(val, VALUE);
        setOperation(Operation.LESS_THAN, val, null);
        return this;
    }

    @Override
    public CaseThen<R> gt(L val) {
        Checker.assertNotNull(val, VALUE);
        setOperation(Operation.GREATER_THAN, val, null);
        return this;
    }

    @Override
    public CaseThen<R> le(L val) {
        Checker.assertNotNull(val, VALUE);
        setOperation(Operation.LESS_EQUALS, val, null);
        return this;
    }

    @Override
    public CaseThen<R> ge(L val) {
        Checker.assertNotNull(val, VALUE);
        setOperation(Operation.GREATER_EQUALS, val, null);
        return this;
    }

    @Override
    public CaseThen<R> equalsTo(L val) {
        return eq(val);
    }

    @Override
    public CaseThen<R> notEqualsTo(L val) {
        return ne(val);
    }

    @Override
    public CaseThen<R> lessThan(L val) {
        return lt(val);
    }

    @Override
    public CaseThen<R> greaterThan(L val) {
        return gt(val);
    }

    @Override
    public CaseThen<R> lessOrEqualsTo(L val) {
        return le(val);
    }

    @Override
    public CaseThen<R> greaterOrEqualsTo(L val) {
        return ge(val);
    }

    @Override
    public CaseThen<R> in(Iterable<L> iterable) {
        Checker.assertNotNull(iterable, "iterable");
        setOperation(Operation.IN, null, iterable);
        return this;
    }

    @Override
    public CaseThen<R> notIn(Iterable<L> iterable) {
        Checker.assertNotNull(iterable, "iterable");
        setOperation(Operation.NOT_IN, null, iterable);
        return this;
    }

    @Override
    public CaseThen<R> in(SelectedWhere<?> subQuery) {
        Checker.assertNotNull(subQuery, SUB_QUERY);
        setOperation(Operation.IN, assertIsSubQuery(subQuery));
        return this;
    }

    @Override
    public CaseThen<R> notIn(SelectedWhere<?> subQuery) {
        Checker.assertNotNull(subQuery, SUB_QUERY);
        setOperation(Operation.NOT_IN, assertIsSubQuery(subQuery));
        return this;
    }

    @Override
    public CaseThen<R> in(Selected<?> subQuery) {
        Checker.assertNotNull(subQuery, SUB_QUERY);
        setOperation(Operation.IN, assertIsSubQuery(subQuery));
        return this;
    }

    @Override
    public CaseThen<R> notIn(Selected<?> subQuery) {
        Checker.assertNotNull(subQuery, SUB_QUERY);
        setOperation(Operation.NOT_IN, assertIsSubQuery(subQuery));
        return this;
    }

    @Override
    public CaseThen<R> isNull() {
        setOperation(Operation.IS_NULL, null, null);
        return this;
    }

    @Override
    public CaseThen<R> isNotNull() {
        setOperation(Operation.IS_NOT_NULL, null, null);
        return this;
    }

    @Override
    public CaseThen<R> like(LikeType type, String val) {
        Checker.assertNotNull(type, "likeType");
        Checker.assertNotNull(val, VALUE);
        setOperation(Operation.LIKE, val, null);
        this.lastWhen.setLikeType(type);
        return this;
    }

    @Override
    public CaseThen<R> notLike(LikeType type, String val) {
        Checker.assertNotNull(type, "likeType");
        Checker.assertNotNull(val, VALUE);
        setOperation(Operation.NOT_LIKE, val, null);
        this.lastWhen.setLikeType(type);
        return this;
    }

    private void setOperation(Operation operation, WithSubQuerySupport subQuery) {
        this.lastWhen.setOperation(operation);
        this.lastWhen.setSubQuery(subQuery);
    }

    private WithSubQuerySupport assertIsSubQuery(Object obj) {
        if (!(obj instanceof WithSubQuerySupport)) {
            throw new IllegalArgumentException("Can't use Sub Query that is not instance of WithSubQuerySupport ! Please use QueryBuilder.subQuery Method");
        }

        return ((WithSubQuerySupport) obj);
    }

    private void setOperation(Operation operation, Object val, Iterable<?> iterable) {
        this.lastWhen.setOperation(operation);
        this.lastWhen.setValue(val);
        if (iterable != null) {
            this.lastWhen.setValues(iterable);
        }
    }

    @Override
    public Pair<String, List<SqlParameter>> doBuild(String table, boolean caseInsensitiveLike) {
        List<SqlParameter> values = new ArrayList<>();
        StringBuilder builder = new StringBuilder("CASE");
        for (WhenImpl when : whens) {
            builder.append(" ")
                    .append(when.asString(table, caseInsensitiveLike));
            values.addAll(when.getAllValues());
        }
        builder.append(" END");

        return new Pair<>(builder.toString(), values);
    }
}
