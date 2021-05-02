package io.github.ulisse1996.entity.relationship;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.spi.DelegatesService;
import io.github.ulisse1996.spi.QueryRunner;

public class RemoveEvent extends PreApplyEvent {

    @Override
    public <T> void apply(T entity) {
        doPreApply(entity, BaseDao::delete);
        QueryRunner.getInstance(entity.getClass())
                .delete(DelegatesService.getInstance().getDeleteSql(entity.getClass()),
                        DelegatesService.getInstance().asWhere(entity).asSqlParameters());
    }

    @Override
    public <T> T applyAndReturn(T entity) {
        throw new UnsupportedOperationException("ApplyAndReturn is not implemented for PersistEvent");
    }
}
