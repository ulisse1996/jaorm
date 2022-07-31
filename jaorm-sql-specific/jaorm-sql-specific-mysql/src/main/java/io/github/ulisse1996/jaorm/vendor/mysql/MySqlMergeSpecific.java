package io.github.ulisse1996.jaorm.vendor.mysql;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.specific.MergeSpecific;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MySqlMergeSpecific extends MergeSpecific {

    @Override
    public String fromUsing() {
        return "";
    }

    @Override
    public String appendAdditionalSql() {
        return "";
    }

    @Override
    public boolean isStandardMerge() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void executeAlternativeMerge(Class<T> klass, Map<SqlColumn<T, ?>, ?> usingColumns,
                                            List<SqlColumn<T, ?>> onColumns, T updateEntity, T insertEntity) {
        EntityDelegate<T> delegate = (EntityDelegate<T>) DelegatesService.getInstance().searchDelegate(klass).get();
        String insert = delegate.getInsertSql();
        EntityMapper<T> entityMapper = delegate.getEntityMapper();
        List<EntityMapper.ColumnMapper<T>> columns = entityMapper.getMappers()
                .stream()
                .filter(m -> !m.isKey())
                .collect(Collectors.toList());
        List<String> names = columns.stream()
                .map(EntityMapper.ColumnMapper::getName)
                .collect(Collectors.toList());
        String update = names.stream()
                .map(n -> String.format("%s = ?", n))
                .collect(Collectors.joining(", "));
        String builder = insert +
                " ON DUPLICATE KEY UPDATE " +
                update;
        List<SqlParameter> parameters = new ArrayList<>(DelegatesService.getInstance().asInsert(insertEntity).asSqlParameters());
        parameters.addAll(entityMapper.getValues(updateEntity, columns).asSqlParameters());
        QueryRunner.getInstance(klass)
                .update(builder, parameters);
    }
}
