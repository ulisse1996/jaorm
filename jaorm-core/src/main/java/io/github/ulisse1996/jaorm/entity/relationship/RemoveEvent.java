package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.entity.event.PostRemove;
import io.github.ulisse1996.jaorm.entity.event.PreRemove;
import io.github.ulisse1996.jaorm.exception.RemoveEventException;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

public class RemoveEvent extends PreApplyEvent {

    @Override
    public <T> void apply(T entity) {
        if (entity instanceof PreRemove<?>) {
            try {
                ((PreRemove<?>) entity).preRemove();
            } catch (Exception ex) {
                throw new RemoveEventException(ex);
            }
        }
        if (hasRelationshipsWithLinkedId(entity)) {
            doPreApply(entity, BaseDao::delete, false, EntityEventType.REMOVE);
            QueryRunner.getInstance(entity.getClass())
                    .delete(DelegatesService.getInstance().getDeleteSql(entity.getClass()),
                            DelegatesService.getInstance().asWhere(entity).asSqlParameters());
        } else {
            QueryRunner.getInstance(entity.getClass())
                    .delete(DelegatesService.getInstance().getDeleteSql(entity.getClass()),
                            DelegatesService.getInstance().asWhere(entity).asSqlParameters());
            doPreApply(entity, BaseDao::delete, false, EntityEventType.REMOVE);
        }
        if (entity instanceof PostRemove<?>) {
            try {
                ((PostRemove<?>) entity).postRemove();
            } catch (Exception ex) {
                throw new RemoveEventException(ex);
            }
        }
    }

    @Override
    public <T> T applyAndReturn(T entity) {
        throw new UnsupportedOperationException("ApplyAndReturn is not implemented for RemoveEvent");
    }
}
