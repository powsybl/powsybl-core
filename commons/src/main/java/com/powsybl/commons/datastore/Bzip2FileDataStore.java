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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import com.powsybl.commons.datasource.DataSourceUtil;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class Bzip2FileDataStore implements DataStore {

    private final Path path;

    public Bzip2FileDataStore(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    @Override
    public List<String> getEntryNames() throws IOException {
        return Collections.singletonList(getEntryName());
    }

    @Override
    public boolean exists(String entryName) {
        return getEntryName().equals(entryName);
    }

    @Override
    public InputStream newInputStream(String entryName) throws IOException {
        return new BZip2CompressorInputStream(Files.newInputStream(path));
    }

    @Override
    public OutputStream newOutputStream(String entryName, boolean append) throws IOException {
        return new BZip2CompressorOutputStream(Files.newOutputStream(path, DataSourceUtil.getOpenOptions(append)));
    }

    private String getEntryName() {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf(".bz2"));
    }

}
