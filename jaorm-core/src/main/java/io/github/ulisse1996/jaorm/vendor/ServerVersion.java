package io.github.ulisse1996.jaorm.vendor;

import java.util.Objects;

public class ServerVersion {

    private final int major;
    private final int minor;
    private final int patch;

    private ServerVersion(String version) {
        Objects.requireNonNull(version, "Version can't be null !");
        version = version.trim();
        int spaceIndex = version.indexOf(" ");
        if (spaceIndex != -1) {
            version = version.substring(0, spaceIndex);
        }
        String[] parts = version.split("\\.");
        this.major = Integer.parseInt(parts[0]);
        if (parts.length > 1) {
            this.minor = Integer.parseInt(parts[1]);
            if (parts.length > 2) {
                this.patch = Integer.parseInt(parts[2]);
            } else {
                this.patch = 0;
            }
        } else {
            this.minor = 0;
            this.patch = 0;
        }
    }

    public static ServerVersion fromString(String version) {
        return new ServerVersion(version);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }
}
