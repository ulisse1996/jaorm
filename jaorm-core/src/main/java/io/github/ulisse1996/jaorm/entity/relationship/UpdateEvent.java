package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.event.PostUpdate;
import io.github.ulisse1996.jaorm.entity.event.PreUpdate;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.exception.PersistEventException;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateEvent extends PreApplyEvent {

    @Override
    public <T> void apply(T entity) {
        doPreApply(entity, (dao, i) -> {
            Object obj = dao.update(i);
            int val = 0;
            if (obj instanceof EntityDelegate<?>) {
                val = ((EntityDelegate<?>) obj).getAndResetUpdateRow();
            }
            return val;
        }, true);
        if (entity instanceof PreUpdate<?>) {
            try {
                ((PreUpdate<?>) entity).preUpdate();
            } catch (Exception ex) {
                throw new PersistEventException(ex);
            }
        }
        List<SqlParameter> parameterList =
                Stream.concat(DelegatesService.getInstance().asArguments(entity).asSqlParameters().stream(),
                        DelegatesService.getInstance().asWhere(entity).asSqlParameters().stream())
                .collect(Collectors.toList());
        QueryRunner.getInstance(entity.getClass())
                .update(DelegatesService.getInstance().getUpdateSql(entity.getClass()), parameterList);
        if (entity instanceof PostUpdate<?>) {
            try {
                ((PostUpdate<?>) entity).postUpdate();
            } catch (Exception ex) {
                throw new PersistEventException(ex);
            }
        }
    }

    @Override
    public <T> T applyAndReturn(T entity) {
        throw new UnsupportedOperationException("ApplyAndReturn is not implemented for PersistEvent");
    }
}
