package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.config.WhereChecker;
import io.github.ulisse1996.jaorm.dsl.query.common.Selected;
import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.CaseEnd;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateWhere;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public class WhereImpl<T, R> extends AbstractWhereImpl<T, R> implements IntermediateWhere<T, R> {

    private final SelectedImpl<T, ?> parent;

    public WhereImpl(SqlColumn<?, R> column, SelectedImpl<T, ?> parent, boolean or, String alias) {
        super(column, or, alias);
        this.parent = parent;
    }

    @Override
    protected String getTable() {
        return parent.getTable();
    }

    @Override
    public SelectedWhere<T> eq(R val) {
        return operation(val, Operation.EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> ne(R val) {
        return operation(val, Operation.NOT_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> lt(R val) {
        return operation(val, Operation.LESS_THAN, this.parent);
    }

    @Override
    public SelectedWhere<T> gt(R val) {
        return operation(val, Operation.GREATER_THAN, this.parent);
    }

    @Override
    public SelectedWhere<T> le(R val) {
        return operation(val, Operation.LESS_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> ge(R val) {
        return operation(val, Operation.GREATER_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> equalsTo(R val) {
        return operation(val, Operation.EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> notEqualsTo(R val) {
        return operation(val, Operation.NOT_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> lessThan(R val) {
        return operation(val, Operation.LESS_THAN, this.parent);
    }

    @Override
    public SelectedWhere<T> greaterThan(R val) {
        return operation(val, Operation.GREATER_THAN, this.parent);
    }

    @Override
    public SelectedWhere<T> lessOrEqualsTo(R val) {
        return operation(val, Operation.LESS_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> greaterOrEqualsTo(R val) {
        return operation(val, Operation.GREATER_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> in(Iterable<R> iterable) {
        this.valid = this.parent.getChecker().isValidWhere(this.column, op, iterable);
        this.op = Operation.IN;
        this.iterable = iterable;
        return this.parent;
    }

    @Override
    public SelectedWhere<T> notIn(Iterable<R> iterable) {
        this.valid = this.parent.getChecker().isValidWhere(this.column, op, iterable);
        this.op = Operation.NOT_IN;
        this.iterable = iterable;
        return this.parent;
    }

    @Override
    public SelectedWhere<T> in(Selected<?> subQuery) {
        this.valid = this.parent.getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public SelectedWhere<T> notIn(Selected<?> subQuery) {
        this.valid = this.parent.getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.NOT_IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public SelectedWhere<T> in(SelectedWhere<?> subQuery) {
        this.valid = this.parent.getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public SelectedWhere<T> notIn(SelectedWhere<?> subQuery) {
        this.valid = this.parent.getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.NOT_IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public SelectedWhere<T> isNull() {
        this.op = Operation.IS_NULL;
        return this.parent;
    }

    @Override
    public SelectedWhere<T> isNotNull() {
        this.op = Operation.IS_NOT_NULL;
        return this.parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedWhere<T> like(LikeType type, String val) {
        assertIsString();
        this.likeType = type;
        return operation((R) val, Operation.LIKE, this.parent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelectedWhere<T> notLike(LikeType type, String val) {
        assertIsString();
        this.likeType = type;
        return operation((R) val, Operation.NOT_LIKE, this.parent);
    }

    @Override
    protected WhereChecker getChecker() {
        return this.parent.getChecker();
    }

    @Override
    public SelectedWhere<T> eq(CaseEnd<R> val) {
        return operation(val, Operation.EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> ne(CaseEnd<R> val) {
        return operation(val, Operation.NOT_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> lt(CaseEnd<R> val) {
        return operation(val, Operation.LESS_THAN, this.parent);
    }

    @Override
    public SelectedWhere<T> gt(CaseEnd<R> val) {
        return operation(val, Operation.GREATER_THAN, this.parent);
    }

    @Override
    public SelectedWhere<T> le(CaseEnd<R> val) {
        return operation(val, Operation.LESS_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> ge(CaseEnd<R> val) {
        return operation(val, Operation.GREATER_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> equalsTo(CaseEnd<R> val) {
        return operation(val, Operation.EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> notEqualsTo(CaseEnd<R> val) {
        return operation(val, Operation.NOT_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> lessThan(CaseEnd<R> val) {
        return operation(val, Operation.LESS_THAN, this.parent);
    }

    @Override
    public SelectedWhere<T> greaterThan(CaseEnd<R> val) {
        return operation(val, Operation.GREATER_THAN, this.parent);
    }

    @Override
    public SelectedWhere<T> lessOrEqualsTo(CaseEnd<R> val) {
        return operation(val, Operation.LESS_EQUALS, this.parent);
    }

    @Override
    public SelectedWhere<T> greaterOrEqualsTo(CaseEnd<R> val) {
        return operation(val, Operation.GREATER_EQUALS, this.parent);
    }
}
