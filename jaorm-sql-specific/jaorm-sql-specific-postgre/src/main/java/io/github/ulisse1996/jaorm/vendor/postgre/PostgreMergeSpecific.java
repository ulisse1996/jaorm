package io.github.ulisse1996.jaorm.vendor.postgre;

import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.vendor.ServerVersion;
import io.github.ulisse1996.jaorm.vendor.specific.MergeSpecific;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PostgreMergeSpecific extends MergeSpecific {

    private static final Singleton<ServerVersion> SERVER_VERSION_SINGLETON = Singleton.instance();

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
        synchronized (PostgreMergeSpecific.class) {

            if (!SERVER_VERSION_SINGLETON.isPresent()) {
                SERVER_VERSION_SINGLETON.set(ServerVersion.fromString(fetchVersion()));
            }

            return SERVER_VERSION_SINGLETON.get().getMajor() >= 15;
        }
    }

    private String fetchVersion() {
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT version()");
             ResultSet rs = preparedStatement.executeQuery()) {
            rs.next();
            return rs.getString(1).replace("PostgreSQL ", "");
        } catch (Exception ex) {
            throw new IllegalStateException("Can't read version !", ex);
        }
    }

    @Override
    public <T> void executeAlternativeMerge(Class<T> klass, Map<SqlColumn<T, ?>, ?> usingColumns,
                                            List<SqlColumn<T, ?>> onColumns, T updateEntity, T insertEntity) {
        EntityDelegate<?> delegate = DelegatesService.getInstance().searchDelegate(klass).get();
        List<SqlParameter> parameters = new ArrayList<>();
        String insert = delegate.getInsertSql();
        String update = delegate.getUpdateSql().replace(
                String.format("UPDATE %s ", delegate.getTable()),
                ""
        );
        String ons = onColumns.stream()
                .map(SqlColumn::getName)
                .collect(Collectors.joining(","));
        String builder = insert +
                " ON CONFLICT (" +
                ons +
                ") DO UPDATE " +
                update;
        parameters.addAll(DelegatesService.getInstance().asInsert(insertEntity).asSqlParameters());
        parameters.addAll(DelegatesService.getInstance().asArguments(updateEntity).asSqlParameters());
        QueryRunner.getInstance(klass)
                .update(builder, parameters);
    }
}
