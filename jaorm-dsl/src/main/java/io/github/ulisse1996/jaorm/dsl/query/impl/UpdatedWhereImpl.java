package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.dsl.config.WhereChecker;
import io.github.ulisse1996.jaorm.dsl.query.common.Selected;
import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.UpdatedWhere;
import io.github.ulisse1996.jaorm.dsl.query.common.intermediate.IntermediateUpdatedWhere;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public class UpdatedWhereImpl<T, R> extends AbstractWhereImpl<T,R> implements IntermediateUpdatedWhere<T, R> {
    private final UpdatedImpl<T> parent;

    public UpdatedWhereImpl(SqlColumn<?, R> column, UpdatedImpl<T> parent, boolean or) {
        super(column, or, null);
        this.parent = parent;
    }

    @Override
    public UpdatedWhere<T> eq(R val) {
        return operation(val, Operation.EQUALS, this.parent);
    }

    @Override
    public UpdatedWhere<T> ne(R val) {
        return operation(val, Operation.NOT_EQUALS, this.parent);
    }

    @Override
    public UpdatedWhere<T> lt(R val) {
        return operation(val, Operation.LESS_THAN, this.parent);
    }

    @Override
    public UpdatedWhere<T> gt(R val) {
        return operation(val, Operation.GREATER_THAN, this.parent);
    }

    @Override
    public UpdatedWhere<T> le(R val) {
        return operation(val, Operation.LESS_EQUALS, this.parent);
    }

    @Override
    public UpdatedWhere<T> ge(R val) {
        return operation(val, Operation.GREATER_EQUALS, this.parent);
    }

    @Override
    public UpdatedWhere<T> equalsTo(R val) {
        return operation(val, Operation.EQUALS, this.parent);
    }

    @Override
    public UpdatedWhere<T> notEqualsTo(R val) {
        return operation(val, Operation.NOT_EQUALS, this.parent);
    }

    @Override
    public UpdatedWhere<T> lessThan(R val) {
        return operation(val, Operation.LESS_THAN, this.parent);
    }

    @Override
    public UpdatedWhere<T> greaterThan(R val) {
        return operation(val, Operation.GREATER_THAN, this.parent);
    }

    @Override
    public UpdatedWhere<T> lessOrEqualsTo(R val) {
        return operation(val, Operation.LESS_EQUALS, this.parent);
    }

    @Override
    public UpdatedWhere<T> greaterOrEqualsTo(R val) {
        return operation(val, Operation.GREATER_EQUALS, this.parent);
    }

    @Override
    public UpdatedWhere<T> in(Iterable<R> iterable) {
        this.valid = this.getChecker().isValidWhere(this.column, op, iterable);
        this.op = Operation.IN;
        this.iterable = iterable;
        return this.parent;
    }

    @Override
    public UpdatedWhere<T> notIn(Iterable<R> iterable) {
        this.valid = this.getChecker().isValidWhere(this.column, op, iterable);
        this.op = Operation.NOT_IN;
        this.iterable = iterable;
        return this.parent;
    }

    @Override
    public UpdatedWhere<T> in(SelectedWhere<?> subQuery) {
        this.valid = this.getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public UpdatedWhere<T> notIn(SelectedWhere<?> subQuery) {
        this.valid = this.getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.NOT_IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public UpdatedWhere<T> in(Selected<?> subQuery) {
        this.valid = this.getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public UpdatedWhere<T> notIn(Selected<?> subQuery) {
        this.valid = this.getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.NOT_IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public UpdatedWhere<T> isNull() {
        this.op = Operation.IS_NULL;
        return this.parent;
    }

    @Override
    public UpdatedWhere<T> isNotNull() {
        this.op = Operation.IS_NOT_NULL;
        return this.parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public UpdatedWhere<T> like(LikeType type, String val) {
        assertIsString();
        this.likeType = type;
        return operation((R) val, Operation.LIKE, this.parent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public UpdatedWhere<T> notLike(LikeType type, String val) {
        assertIsString();
        this.likeType = type;
        return operation((R) val, Operation.NOT_LIKE, this.parent);
    }

    @Override
    protected WhereChecker getChecker() {
        return this.parent.getConfig().getChecker();
    }

    @Override
    protected String getTable() {
        return this.parent.getTable();
    }
}
