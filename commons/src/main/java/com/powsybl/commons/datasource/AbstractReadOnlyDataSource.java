/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractReadOnlyDataSource implements ReadOnlyDataSource {

    public boolean exists(String suffix, String ext) throws IOException {
        return exists(suffix, ext, false);
    }

    public boolean exists(String fileName)throws IOException {
        return exists(fileName, false);
    }

    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return newInputStream(suffix, ext, false);
    }

    public InputStream newInputStream(String fileName) throws IOException {
        return newInputStream(fileName, false);
    }

    public boolean isConsistentWithDataSource(String fileName) {
        return fileName.startsWith(getBaseName());
    }
}
