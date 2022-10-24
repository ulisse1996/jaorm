package io.github.ulisse1996.jaorm.dsl.query.impl.simple;

import io.github.ulisse1996.jaorm.dsl.config.WhereChecker;
import io.github.ulisse1996.jaorm.dsl.query.common.SelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.enums.LikeType;
import io.github.ulisse1996.jaorm.dsl.query.enums.Operation;
import io.github.ulisse1996.jaorm.dsl.query.impl.AbstractWhereImpl;
import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.IntermediateSimpleWhere;
import io.github.ulisse1996.jaorm.dsl.query.simple.intermediate.SimpleSelectedWhere;
import io.github.ulisse1996.jaorm.dsl.query.simple.trait.WithProjectionResult;
import io.github.ulisse1996.jaorm.entity.SqlColumn;

public class SimpleWhereImpl<R> extends AbstractWhereImpl<Object, R> implements IntermediateSimpleWhere<R> {

    private final SimpleSelectedImpl parent;

    protected SimpleWhereImpl(SqlColumn<?, R> column, SimpleSelectedImpl parent, boolean or, String alias) {
        super(column, or, alias);
        this.parent = parent;
    }

    @Override
    public SimpleSelectedWhere eq(R val) {
        return operation(val, Operation.EQUALS, this.parent);
    }

    @Override
    public SimpleSelectedWhere ne(R val) {
        return operation(val, Operation.NOT_EQUALS, this.parent);
    }

    @Override
    public SimpleSelectedWhere lt(R val) {
        return operation(val, Operation.LESS_THAN, this.parent);
    }

    @Override
    public SimpleSelectedWhere gt(R val) {
        return operation(val, Operation.GREATER_THAN, this.parent);
    }

    @Override
    public SimpleSelectedWhere le(R val) {
        return operation(val, Operation.LESS_EQUALS, this.parent);
    }

    @Override
    public SimpleSelectedWhere ge(R val) {
        return operation(val, Operation.GREATER_EQUALS, this.parent);
    }

    @Override
    public SimpleSelectedWhere equalsTo(R val) {
        return operation(val, Operation.EQUALS, this.parent);
    }

    @Override
    public SimpleSelectedWhere notEqualsTo(R val) {
        return operation(val, Operation.NOT_EQUALS, this.parent);
    }

    @Override
    public SimpleSelectedWhere lessThan(R val) {
        return operation(val, Operation.LESS_THAN, this.parent);
    }

    @Override
    public SimpleSelectedWhere greaterThan(R val) {
        return operation(val, Operation.GREATER_THAN, this.parent);
    }

    @Override
    public SimpleSelectedWhere lessOrEqualsTo(R val) {
        return operation(val, Operation.LESS_EQUALS, this.parent);
    }

    @Override
    public SimpleSelectedWhere greaterOrEqualsTo(R val) {
        return operation(val, Operation.GREATER_EQUALS, this.parent);
    }

    @Override
    public SimpleSelectedWhere in(Iterable<R> iterable) {
        this.valid = this.parent.getConfiguration().getChecker().isValidWhere(this.column, op, iterable);
        this.op = Operation.IN;
        this.iterable = iterable;
        return this.parent;
    }

    @Override
    public SimpleSelectedWhere notIn(Iterable<R> iterable) {
        this.valid = this.parent.getConfiguration().getChecker().isValidWhere(this.column, op, iterable);
        this.op = Operation.NOT_IN;
        this.iterable = iterable;
        return this.parent;
    }

    @Override
    public SimpleSelectedWhere in(SelectedWhere<?> subQuery) {
        throw new UnsupportedOperationException("Please use WithProjectResult method for SubQuery support");
    }

    @Override
    public SimpleSelectedWhere notIn(SelectedWhere<?> subQuery) {
        throw new UnsupportedOperationException("Please use WithProjectResult method for SubQuery support");
    }

    @Override
    public SimpleSelectedWhere in(WithProjectionResult subQuery) {
        this.valid = this.parent.getConfiguration().getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public SimpleSelectedWhere notIn(WithProjectionResult subQuery) {
        this.valid = this.parent.getConfiguration().getChecker().isValidWhere(this.column, op, subQuery);
        this.op = Operation.NOT_IN;
        this.subQuery = assertIsSubQuery(subQuery);
        return this.parent;
    }

    @Override
    public SimpleSelectedWhere isNull() {
        this.op = Operation.IS_NULL;
        return this.parent;
    }

    @Override
    public SimpleSelectedWhere isNotNull() {
        this.op = Operation.IS_NOT_NULL;
        return this.parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SimpleSelectedWhere like(LikeType type, String val) {
        assertIsString();
        this.likeType = type;
        return operation((R) val, Operation.LIKE, this.parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SimpleSelectedWhere notLike(LikeType type, String val) {
        assertIsString();
        this.likeType = type;
        return operation((R) val, Operation.NOT_LIKE, this.parent);
    }

    @Override
    protected WhereChecker getChecker() {
        return this.parent.getConfiguration().getChecker();
    }

    @Override
    protected String getTable() {
        return null;
    }
}
