/**
 * Copyright (c) 2017, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Giovanni Ferrari <giovanni.ferrari@techrain.it>
 */
public class Bzip2MemDataSource extends ReadOnlyMemDataSource {

    Bzip2MemDataSource(String fileName, InputStream content) {
        super(DataSourceUtil.getBaseName(fileName));

        String zipped = fileName.substring(0, fileName.lastIndexOf('.'));
        putData(zipped, content);
    }

    private InputStream getCompressedInputStream(InputStream is) throws IOException {
        return new BZip2CompressorInputStream(new BufferedInputStream(is));
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return newInputStream(DataSourceUtil.getFileName(getBaseName(), suffix, ext));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        return getCompressedInputStream(super.newInputStream(fileName));
    }
}
