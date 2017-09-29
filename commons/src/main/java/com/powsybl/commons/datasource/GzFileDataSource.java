/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GzFileDataSource extends FileDataSource {

    public GzFileDataSource(Path directory, String baseName, DataSourceObserver observer) {
        super(directory, baseName, observer);
    }

    public GzFileDataSource(Path directory, String baseName) {
        super(directory, baseName);
    }

    @Override
    protected String getCompressionExt() {
        return ".gz";
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
