/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

import java.io.*;
import java.nio.file.Path;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class XZDirectoryDataSource extends DirectoryDataSource {

    public XZDirectoryDataSource(Path directory, String baseName,
                                 String mainExtension,
                                 DataSourceObserver observer) {
        super(directory, baseName, CompressionFormat.XZ, mainExtension, observer);
    }

    @Override
    protected InputStream getCompressedInputStream(InputStream is) throws IOException {
        return new XZCompressorInputStream(new BufferedInputStream(is));
    }

    @Override
    protected OutputStream getCompressedOutputStream(OutputStream os) throws IOException {
        return new XZCompressorOutputStream(new BufferedOutputStream(os));
    }
}
