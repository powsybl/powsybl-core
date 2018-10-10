package com.powsybl.commons.config;

public class VersionConfig {

    private final String[] version;

    public VersionConfig(String version) {
        this.version = version.split("\\.");
    }

    public boolean isStrictlyOlderThan(String version) {
        String[] otherVersion = version.split("\\.");
        for (int i = 0; i < Math.min(this.version.length, otherVersion.length); i++) {
            int thisV = Integer.parseInt(this.version[i]);
            int otherV = Integer.parseInt(otherVersion[i]);
            if (thisV != otherV) {
                return thisV < otherV;
            }
        }
        return this.version.length < otherVersion.length;
    }

    public boolean equalsOrIsNewerThan(String version) {
        String[] otherVersion = version.split("\\.");
        for (int i = 0; i < Math.min(this.version.length, otherVersion.length); i++) {
            int thisV = Integer.parseInt(this.version[i]);
            int otherV = Integer.parseInt(otherVersion[i]);
            if (thisV != otherV) {
                return thisV > otherV;
            }
        }
        return this.version.length >= otherVersion.length;
    }

    public String toString() {
        return String.join(".", this.version);
    }

}
