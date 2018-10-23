/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum Bzip2DataSourceCompressor implements DataSourceCompressor {

    INSTANCE;

    @Override
    public String getExtension() {
        return '.' + CompressionFormat.BZIP2.getExtension();
    }

    @Override
    public InputStream uncompress(InputStream is) throws IOException {
        return new BZip2CompressorInputStream(new BufferedInputStream(is));
    }

    @Override
    public OutputStream compress(OutputStream os) throws IOException {
        return new BZip2CompressorOutputStream(new BufferedOutputStream(os));
    }
}
