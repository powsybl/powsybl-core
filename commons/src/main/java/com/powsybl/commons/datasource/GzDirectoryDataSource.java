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
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class GzDirectoryDataSource extends DirectoryDataSource {

    public GzDirectoryDataSource(Path directory, String baseName,
                                 String mainExtension,
                                 DataSourceObserver observer) {
        super(directory, baseName, CompressionFormat.GZIP, mainExtension, observer);
    }

    @Override
    protected InputStream getCompressedInputStream(InputStream is) throws IOException {
        return new GZIPInputStream(is);
    }

    @Override
    protected OutputStream getCompressedOutputStream(OutputStream os) throws IOException {
        return new GZIPOutputStream(os);
    }
}
