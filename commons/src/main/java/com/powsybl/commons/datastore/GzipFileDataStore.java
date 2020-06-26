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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.GzFileDataSource;
import com.powsybl.commons.util.Filenames;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class GzipFileDataStore implements DataStore {

    private final Path path;
    private String entryFilename;

    public GzipFileDataStore(Path path) {
        this.path = Objects.requireNonNull(path);
        entryFilename = Filenames.getBasename(path.getFileName().toString());
    }

    @Override
    public List<String> getEntryNames() throws IOException {
        return Collections.singletonList(entryFilename);
    }

    @Override
    public boolean exists(String entryName) {
        return entryFilename.equals(entryName);
    }

    @Override
    public InputStream newInputStream(String entryName) throws IOException {
        return new GZIPInputStream(Files.newInputStream(path));
    }

    @Override
    public OutputStream newOutputStream(String entryName, boolean append) throws IOException {
        return new GZIPOutputStream(Files.newOutputStream(path, DataSourceUtil.getOpenOptions(append)));
    }

    @Override
    public DataSource toDataSource(String filename) {
        return new GzFileDataSource(path.getParent(), filename);
    }

}
