package io.jaorm.intellij.plugin.service.impl;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Transient;
import io.jaorm.intellij.plugin.service.ConnectionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@State(name = "jaorm-datasource", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class JaormDatasourceService {

    @Attribute
    String username;

    @Attribute
    String password;

    @Attribute
    String url;

    @Attribute
    String driverPath;

    @Transient
    private ConnectionInfo connectionInfo;

    public @Nullable JaormDatasourceService getState() {
        return this;
    }

    public void loadState(@NotNull JaormDatasourceService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean testSql(String username, String password, String url, @NotNull String driverPath) {
        ConnectionInfo currInfo = new ConnectionInfo(username, password, url, driverPath);
        currInfo.registerDriver();
        try (Connection connection = DriverManager.getConnection(currInfo.getUrl(),
                currInfo.getUsername(), currInfo.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM MYTABLE")) {
            preparedStatement.execute();
        } catch (SQLException ex) {
            return ex.getMessage() != null && ex.getMessage().contains("MYTABLE");
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    public synchronized ConnectionInfo getConnectionInfo() {
        if (this.connectionInfo == null && this.driverPath != null) {
            this.connectionInfo = new ConnectionInfo(this.username, this.password, this.url, this.driverPath);
        }
        return this.connectionInfo;
    }

    public void setConnectionInfo(String username, String password, String url, String driver) {
        this.connectionInfo = new ConnectionInfo(username, password, url, driver);
        this.username = username;
        this.password = password;
        this.url = url;
        this.driverPath = driver;
    }

    public boolean isInitialized() {
        return this.connectionInfo != null;
    }
}
