package io.github.ulisse1996.entity.relationship;

import io.github.ulisse1996.BaseDao;
import io.github.ulisse1996.entity.sql.SqlParameter;
import io.github.ulisse1996.spi.DelegatesService;
import io.github.ulisse1996.spi.QueryRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateEvent extends PreApplyEvent {

    @Override
    public <T> void apply(T entity) {
        doPreApply(entity, BaseDao::update);
        List<SqlParameter> parameterList =
                Stream.concat(DelegatesService.getInstance().asArguments(entity).asSqlParameters().stream(),
                        DelegatesService.getInstance().asWhere(entity).asSqlParameters().stream())
                .collect(Collectors.toList());
        QueryRunner.getInstance(entity.getClass())
                .update(DelegatesService.getInstance().getUpdateSql(entity.getClass()), parameterList);
    }

    @Override
    public <T> T applyAndReturn(T entity) {
        throw new UnsupportedOperationException("ApplyAndReturn is not implemented for PersistEvent");
    }
}
