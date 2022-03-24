/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import org.scijava.nativelib.NativeLoader;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class JniDatabaseReader implements DatabaseReader {

    static {
        try {
            NativeLoader.loadLibrary("powsybl-powerfactory-db-native");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public native void read(String powerFactoryHome, String projectName, DataObjectBuilder builder);
}
