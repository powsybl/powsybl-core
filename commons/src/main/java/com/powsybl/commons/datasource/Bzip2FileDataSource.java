/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.*;
import java.nio.file.Path;


/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class Bzip2FileDataSource extends FileDataSource {

    public Bzip2FileDataSource(Path directory, String baseName, DataSourceObserver observer) {
        super(directory, baseName, observer);
    }

    public Bzip2FileDataSource(Path directory, String baseName) {
        super(directory, baseName);
    }

    @Override
    protected String getCompressionExt() {
        return ".bz2";
    }

    @Override
    protected InputStream getCompressedInputStream(InputStream is) throws IOException {
        return new BZip2CompressorInputStream(new BufferedInputStream(is));
    }

    @Override
    protected OutputStream getCompressedOutputStream(OutputStream os) throws IOException {
        return new BZip2CompressorOutputStream(new BufferedOutputStream(os));
    }
}
