/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import org.scijava.nativelib.NativeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class JniDatabaseReader implements DatabaseReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JniDatabaseReader.class);

    static Boolean ok;

    private static void init() {
        if (ok == null) {
            try {
                NativeLoader.loadLibrary("powsybl-powerfactory-db-native");
                ok = true;
            } catch (IOException e) {
                LOGGER.warn(e.getMessage());
                ok = false;
            }
        }
    }

    @Override
    public boolean isOk() {
        init();
        return ok;
    }

    public native void read(String powerFactoryHomeDir, String projectName, DataObjectBuilder builder);
}
