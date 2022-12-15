/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;

import java.io.*;
import java.nio.file.Path;

/**
 * @author Olivier Bretteville <olivier.bretteville at rte-france.com>
 */
public class ZstdFileDataSource extends FileDataSource {

    public ZstdFileDataSource(Path directory, String baseName, DataSourceObserver observer) {
        super(directory, baseName, observer);
    }

    public ZstdFileDataSource(Path directory, String baseName) {
        super(directory, baseName);
    }

    @Override
    protected String getCompressionExt() {
        return ".zst";
    }

    @Override
    protected InputStream getCompressedInputStream(InputStream is) throws IOException {
        return new ZstdCompressorInputStream(new BufferedInputStream(is));
    }

    @Override
    protected OutputStream getCompressedOutputStream(OutputStream os) throws IOException {
        return new ZstdCompressorOutputStream(new BufferedOutputStream(os));
    }
}
