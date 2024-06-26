/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
 * @author Olivier Bretteville {@literal <olivier.bretteville at rte-france.com>}
 */
public class XZFileDataSource extends FileDataSource {

    public XZFileDataSource(Path directory, String baseName, String baseExtension, DataSourceObserver observer) {
        super(directory, baseName, baseExtension, observer);
    }

    public XZFileDataSource(Path directory, String baseName, String baseExtension) {
        super(directory, baseName, baseExtension, null);
    }

    public XZFileDataSource(Path directory, String baseName, DataSourceObserver observer) {
        super(directory, baseName, "", observer);
    }

    public XZFileDataSource(Path directory, String baseName) {
        super(directory, baseName, "", null);
    }

    @Override
    protected String getCompressionExt() {
        return ".xz";
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
