package io.github.ulisse1996.jaorm.vendor.specific;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MergeSpecific implements Specific {

    public abstract String fromUsing();
    public abstract String appendAdditionalSql();
    public abstract boolean isStandardMerge();
    public abstract <T> void executeAlternativeMerge(Class<T> klass, Map<SqlColumn<T, ?>, ?> usingColumns, List<SqlColumn<T,?>> onColumns,
                                     T updateEntity, T insertEntity);

    @SuppressWarnings("unchecked")
    public <T> void executeMerge(Class<T> klass, Map<SqlColumn<T, ?>, ?> usingColumns, List<SqlColumn<T, ?>> onColumns,
                                  T updateEntity, T insertEntity) {
        if (isStandardMerge()) {
            List<SqlParameter> parameters = new ArrayList<>();
            AliasesSpecific aliasesSpecific = VendorSpecific.getSpecific(AliasesSpecific.class);
            EntityDelegate<T> entityDelegate = (EntityDelegate<T>) DelegatesService.getInstance().searchDelegate(klass).get();
            StringBuilder builder = new StringBuilder("MERGE INTO ")
                    .append(entityDelegate.getTable())
                    .append(" M")
                    .append(" USING ( SELECT ");
            for (Map.Entry<SqlColumn<T, ?>, ?> entry : usingColumns.entrySet()) {
                parameters.add(new SqlParameter(entry.getValue()));
                builder.append("?")
                        .append(aliasesSpecific.convertToAlias(entry.getKey().getName()));
            }
            builder.append(fromUsing())
                    .append(" ) H ON ( ");
            for (int i = 0; i < onColumns.size(); i++) {
                builder.append(i != 0 ? " AND " : "")
                        .append("M.").append(onColumns.get(i).getName())
                        .append(" = ")
                        .append("H.").append(onColumns.get(i).getName());
            }
            builder.append(" )");
            setInsertEntity(insertEntity, parameters, entityDelegate, builder);
            setUpdateEntity(onColumns, updateEntity, parameters, entityDelegate, builder);
            builder.append(appendAdditionalSql());
            QueryRunner.getInstance(klass)
                    .update(builder.toString(), parameters);
        } else {
            executeAlternativeMerge(klass, usingColumns, onColumns, updateEntity, insertEntity);
        }
    }

    private <T> void setInsertEntity(T insertEntity, List<SqlParameter> parameters, EntityDelegate<T> entityDelegate, StringBuilder builder) {
        if (insertEntity != null) {
            String insertSql = entityDelegate.getInsertSql();
            insertSql = insertSql.replace(
                    String.format("INTO %s ", entityDelegate.getTable()),
                    ""
            );
            builder.append(" WHEN NOT MATCHED THEN ")
                    .append(insertSql);
            parameters.addAll(DelegatesService.getInstance().asInsert(insertEntity).asSqlParameters());
        }
    }

    private <T> void setUpdateEntity(List<SqlColumn<T, ?>> onColumns, T updateEntity, List<SqlParameter> parameters, EntityDelegate<T> entityDelegate, StringBuilder builder) {
        if (updateEntity != null) {
            List<String> ons = onColumns.stream().map(SqlColumn::getName).collect(Collectors.toList());
            List<EntityMapper.ColumnMapper<T>> mappers = entityDelegate.getEntityMapper().getMappers()
                    .stream()
                    .filter(el -> !ons.contains(el.getName()))
                    .collect(Collectors.toList());
            builder.append(" WHEN MATCHED THEN ")
                    .append("UPDATE ");
            boolean first = true;
            for (int i = 0; i < mappers.size(); i++) {
                EntityMapper.ColumnMapper<T> mapper = mappers.get(i);
                if (first) {
                    builder.append("SET ");
                    first = false;
                }
                builder.append("M.")
                        .append(mapper.getName())
                        .append(" = ?");
                if (i + 1 < mappers.size()) {
                    builder.append(", ");
                }
            }
            parameters.addAll(
                    entityDelegate.getEntityMapper().getValues(updateEntity, mappers)
                            .asSqlParameters()
            );
        }
    }
}
