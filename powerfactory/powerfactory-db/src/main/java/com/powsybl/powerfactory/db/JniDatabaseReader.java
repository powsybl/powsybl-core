/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.db;

import org.apache.commons.lang3.SystemUtils;
import org.scijava.nativelib.NativeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class JniDatabaseReader implements DatabaseReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JniDatabaseReader.class);

    static Boolean ok;

    private static void init() {
        if (ok == null) {
            if (SystemUtils.IS_OS_WINDOWS) { // PowerFactory is only available on Windows
                try {
                    NativeLoader.loadLibrary("powsybl-powerfactory-db-native");
                    ok = true;
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                    ok = false;
                }
            } else {
                ok = false;
            }
        }
    }

    @Override
    public boolean isOk() {
        init();
        return ok;
    }

    @Override
    public void read(String powerFactoryHomeDir, String projectName, DataObjectBuilder builder) {
        init();
        readNative(powerFactoryHomeDir, projectName, builder);
    }

    private native void readNative(String powerFactoryHomeDir, String projectName, DataObjectBuilder builder);
}
