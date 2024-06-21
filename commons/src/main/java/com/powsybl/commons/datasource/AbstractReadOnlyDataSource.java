/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.io.IOException;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractReadOnlyDataSource implements ReadOnlyDataSource {

    @Override
    public boolean exists(String fileName) throws IOException {
        return checkFileExistence(fileName, false);
    }

    @Override
    public boolean existsStrict(String fileName) throws IOException {
        return checkFileExistence(fileName, true);
    }

    /**
     * Check if a file exists in the archive.
     * @param fileName Name of the file
     * @param checkConsistencyWithDataSource Should the filename be checked for consistency with the DataSource
     * @return true if the file exists, else false
     */
    protected abstract boolean checkFileExistence(String fileName, boolean checkConsistencyWithDataSource) throws IOException;

    public boolean isConsistentWithDataSource(String fileName) {
        return fileName.startsWith(getBaseName());
    }
}
