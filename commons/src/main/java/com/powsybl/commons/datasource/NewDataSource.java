/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.PowsyblException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public interface NewDataSource extends ReadOnlyDataSource {

    OutputStream newOutputStream(String fileName, boolean append) throws IOException;

    OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException;

    static NewDataSource fromPath(Path file) {
        Objects.requireNonNull(file);
        if (!Files.isRegularFile(file)) {
            throw new PowsyblException("File " + file + " does not exist or is not a regular file");
        }
        Path absFile = file.toAbsolutePath();
        return fromPath(absFile.getParent(), absFile.getFileName().toString());
    }

    static NewDataSource fromPath(Path directory, String fileName) {
        return DataSourceUtil.createNewDataSource(directory, fileName);
    }
}
