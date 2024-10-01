/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface DataSource extends ReadOnlyDataSource {

    OutputStream newOutputStream(String fileName, boolean append) throws IOException;

    OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException;

    /**
     * Creates a {@link DataSource} from the given path, throwing an exception if the path does not correspond to an
     * existing file. Note that the basename of the created {@link DataSource} is
     * <ul>
     *     <li>the file name if the file is a directory</li>
     *     <li>the base name guessed by {@link FileInformation} if the file is not a directory</li>
     * </ul>
     */
    static DataSource fromPath(Path file) {
        Objects.requireNonNull(file);
        if (!Files.exists(file)) {
            throw new PowsyblException("File " + file + " does not exist");
        }
        return DataSourceUtil.createDataSource(file, null);
    }
}
