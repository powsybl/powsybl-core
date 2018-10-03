/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

/**
 * Environment variables substitution
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class PlatformEnv {

    private PlatformEnv() {
    }

    public static String substitute(String str) {
        if (str == null) {
            return null;
        }
        String userHome = System.getProperty("user.home");
        String appRoot = System.getProperty("app.root");
        String result = str.replace("$HOME", userHome) // backward compatibility
                .replace("${user.home}", userHome)
                .replace("${user_home}", userHome); // because ${user.home} is already substitute by Maven
        if (appRoot != null) {
            result = result.replace("${app.root}", appRoot);
        }
        return result;
    }
}
