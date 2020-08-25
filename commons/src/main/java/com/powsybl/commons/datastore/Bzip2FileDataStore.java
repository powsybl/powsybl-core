/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import com.powsybl.commons.datasource.DataSourceUtil;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class Bzip2FileDataStore extends AbstractSingleCompressedFileDataStore {

    public Bzip2FileDataStore(Path path) {
        super(path);
    }

    @Override
    public InputStream newInputStream(String entryName) throws IOException {
        if (!exists(entryName)) {
            throw new IOException("Entry name does not exists");
        }
        return new BZip2CompressorInputStream(Files.newInputStream(getPath()));
    }

    @Override
    public OutputStream newOutputStream(String entryName, boolean append) throws IOException {
        if (!getEntryFilename().equals(entryName)) {
            throw new IOException("Entry name does not match");
        }
        return new BZip2CompressorOutputStream(Files.newOutputStream(getPath(), DataSourceUtil.getOpenOptions(append)));
    }

}
