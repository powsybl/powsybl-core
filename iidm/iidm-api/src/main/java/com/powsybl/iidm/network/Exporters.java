/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceObserver;
import com.powsybl.commons.datasource.DataSourceUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A utility class to work with IIDM exporters.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class Exporters {

    private Exporters() {
    }

    public static DataSource createDataSource(Path directory, String fileNameOrBaseName, DataSourceObserver observer) {
        return DataSourceUtil.createDataSource(directory, fileNameOrBaseName, observer);
    }

    public static DataSource createDataSource(Path file, DataSourceObserver observer) {
        Objects.requireNonNull(file);
        if (Files.exists(file) && !Files.isRegularFile(file)) {
            throw new UncheckedIOException(new IOException("File " + file + " already exists and is not a regular file"));
        }
        return DataSourceUtil.createDataSource(file, observer);
    }

    public static DataSource createDataSource(Path file) {
        return createDataSource(file, null);
    }
}
