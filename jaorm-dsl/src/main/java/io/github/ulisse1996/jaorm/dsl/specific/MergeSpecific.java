package io.github.ulisse1996.jaorm.dsl.specific;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.AliasesSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.Specific;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MergeSpecific extends Specific {

    String fromUsing();
    boolean isStandardMerge();
    <T> void executeAlternativeMerge(Class<T> klass, Map<SqlColumn<T, ?>, ?> usingColumns, List<SqlColumn<T,?>> onColumns,
                                     T updateEntity, T insertEntity);

    @SuppressWarnings("unchecked")
    default <T> void executeMerge(Class<T> klass, Map<SqlColumn<T, ?>, ?> usingColumns, List<SqlColumn<T, ?>> onColumns,
                                  T updateEntity, T insertEntity) {
        if (isStandardMerge()) {
            List<SqlParameter> parameters = new ArrayList<>();
            AliasesSpecific aliasesSpecific = VendorSpecific.getSpecific(AliasesSpecific.class);
            EntityDelegate<T> entityDelegate = (EntityDelegate<T>) DelegatesService.getInstance().searchDelegate(klass).get();
            StringBuilder builder = new StringBuilder("MERGE INTO ")
                    .append(entityDelegate.getTable())
                    .append(" USING ( SELECT ");
            for (Map.Entry<SqlColumn<T, ?>, ?> entry : usingColumns.entrySet()) {
                parameters.add(new SqlParameter(entry.getValue()));
                builder.append("?")
                        .append(aliasesSpecific.convertToAlias(entry.getKey().getName()))
                        .append(" ");
            }
            builder.append(fromUsing())
                    .append(" ) H ON ( ");
            for (int i = 0; i < onColumns.size(); i++) {
                builder.append(i != 0 ? " AND " : "")
                        .append(entityDelegate.getTable()).append(".").append(onColumns.get(i).getName())
                        .append(" = ")
                        .append("H.").append(onColumns.get(i).getName());
            }
            if (insertEntity != null) {
                builder.append(" WHEN NOT MATCHED ")
                        .append(entityDelegate.getInsertSql());
                parameters.addAll(DelegatesService.getInstance().asInsert(insertEntity).asSqlParameters());

                QueryRunner.getInstance(klass)
                        .insert(insertEntity, builder.toString(), parameters);
            }
            if (updateEntity != null) {
                builder.append(" WHEN MATCHED ")
                        .append(entityDelegate.getUpdateSql())
                        .append(" ")
                        .append(entityDelegate.getKeysWhere());
                List<SqlParameter> parameterList =
                        Stream.concat(DelegatesService.getInstance().asArguments(updateEntity).asSqlParameters().stream(),
                                        DelegatesService.getInstance().asWhere(updateEntity).asSqlParameters().stream())
                                .collect(Collectors.toList());
                parameters.addAll(parameterList);
                QueryRunner.getInstance(klass)
                        .update(builder.toString(), parameters);
            }
        } else {
            executeAlternativeMerge(klass, usingColumns, onColumns, updateEntity, insertEntity);
        }
    }
}
