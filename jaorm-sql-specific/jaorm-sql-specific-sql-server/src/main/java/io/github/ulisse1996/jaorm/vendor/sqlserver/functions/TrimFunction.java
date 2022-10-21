package io.github.ulisse1996.jaorm.vendor.sqlserver.functions;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import io.github.ulisse1996.jaorm.vendor.ServerVersion;
import io.github.ulisse1996.jaorm.vendor.VendorFunctionWithParams;
import io.github.ulisse1996.jaorm.vendor.util.ArgumentsUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TrimFunction implements VendorFunctionWithParams<String> {

    private static final Singleton<ServerVersion> SERVER_VERSION_SINGLETON = Singleton.instance();
    private static final char SPACE = ' ';

    private final TrimType type;
    private final char character;
    private final Selectable<String> selectable;

    public static TrimFunction trim(Selectable<String> selectable) {
        return trim(null, SPACE, selectable);
    }

    public static TrimFunction trim(char character, Selectable<String> selectable) {
        return trim(null, character, selectable);
    }

    public static TrimFunction trim(TrimType type, Selectable<String> selectable) {
        return trim(type, SPACE, selectable);
    }

    public static TrimFunction trim(TrimType type, char character, Selectable<String> selectable) {
        return new TrimFunction(type, character, selectable);
    }

    private TrimFunction(TrimType type, char character, Selectable<String> selectable) {
        this.type = type;
        this.character = character;
        this.selectable = selectable;
    }

    @Override
    public String apply(String alias) {
        initServerVersion();

        if (SERVER_VERSION_SINGLETON.get().getMajor() < 16) {
            return simpleTrim(alias);
        }

        String t = this.type != null ? this.type.name() : "";
        String c = this.character != SPACE ? String.format(" '%s' FROM ", this.character) : " ' ' FROM ";
        String s = ArgumentsUtils.getColumnName(this.selectable, alias);
        return String.format("TRIM(%s%s%s)", t, c, s);
    }

    private String simpleTrim(String alias) {
        String s = ArgumentsUtils.getColumnName(this.selectable, alias);
        String c = this.character != SPACE ? String.format(" '%s' FROM ", this.character) : " ' ' FROM ";
        if (type == null || TrimType.BOTH.equals(type)) {
            return String.format("TRIM(%s%s)", c, s);
        } else {
            if (this.character != SPACE) {
                throw new IllegalArgumentException("Can't use custom char with LTRIM/RTRIM");
            }
            if (TrimType.LEADING.equals(type)) {
                return String.format("LTRIM(%s)", s);
            } else {
                return String.format("RTRIM(%s)", s);
            }
        }
    }

    private void initServerVersion() {
        synchronized (TrimFunction.class) {

            if (!SERVER_VERSION_SINGLETON.isPresent()) {
                DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement pr = connection.prepareStatement("SELECT SERVERPROPERTY('productversion')");
                     ResultSet rs = pr.executeQuery()) {
                    rs.next();
                    SERVER_VERSION_SINGLETON.set(ServerVersion.fromString(rs.getString(1)));
                } catch (SQLException ex) {
                    throw new IllegalArgumentException("Can't read server version !");
                }
            }
        }
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public List<?> getParams() {
        return ArgumentsUtils.getParams(this.selectable);
    }
}
