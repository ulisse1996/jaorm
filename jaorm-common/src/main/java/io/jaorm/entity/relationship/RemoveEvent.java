package io.jaorm.entity.relationship;

import io.jaorm.BaseDao;
import io.jaorm.spi.DelegatesService;
import io.jaorm.spi.QueryRunner;

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
