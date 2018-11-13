/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ConfigVersion {

    private final String[] version;

    public ConfigVersion(String version) {
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
