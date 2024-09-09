/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class GenericReadOnlyDataSource implements ReadOnlyDataSource {

    private final ReadOnlyDataSource[] dataSources;

    public GenericReadOnlyDataSource(Path directory, String baseName, String dataExtension, DataSourceObserver observer) {
        dataSources = new DataSource[] {
            new DirectoryDataSource(directory, baseName, dataExtension, observer),
            new ZstdDirectoryDataSource(directory, baseName, dataExtension, observer),
            new ZipArchiveDataSource(directory),
            new ZipArchiveDataSource(directory, baseName, dataExtension, observer),
            new TarArchiveDataSource(directory),
            new XZDirectoryDataSource(directory, baseName, dataExtension, observer),
            new GzDirectoryDataSource(directory, baseName, dataExtension, observer),
            new Bzip2DirectoryDataSource(directory, baseName, dataExtension, observer)
        };
    }

    /**
     * The data source contains all files inside the given directory.
     */
    public GenericReadOnlyDataSource(Path directory) {
        this(directory, "", null);
    }

    public GenericReadOnlyDataSource(Path directory, String baseName) {
        this(directory, baseName, null);
    }

    public GenericReadOnlyDataSource(Path directory, String baseName, String dataExtension) {
        this(directory, baseName, dataExtension, null);
    }

    @Override
    public String getBaseName() {
        return dataSources[0].getBaseName();
    }

    @Override
    public String getDataExtension() {
        return dataSources[0].getDataExtension();
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        for (ReadOnlyDataSource dataSource : dataSources) {
            if (dataSource.exists(suffix, ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDataExtension(String ext) {
        for (ReadOnlyDataSource dataSource : dataSources) {
            if (dataSource.isDataExtension(ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        for (ReadOnlyDataSource dataSource : dataSources) {
            if (dataSource.exists(fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        for (ReadOnlyDataSource dataSource : dataSources) {
            if (dataSource.exists(suffix, ext)) {
                return dataSource.newInputStream(suffix, ext);
            }
        }
        throw new IOException(DataSourceUtil.getFileName(getBaseName(), suffix, ext) + " not found");
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        for (ReadOnlyDataSource dataSource : dataSources) {
            if (dataSource.exists(fileName)) {
                return dataSource.newInputStream(fileName);
            }
        }
        throw new IOException(fileName + " not found");
    }

    @Override
    public Set<String> listNames(String regex) throws IOException {
        Set<String> names = new HashSet<>();
        for (ReadOnlyDataSource dataSource : dataSources) {
            try {
                names.addAll(dataSource.listNames(regex));
            } catch (Exception x) {
                // Nothing
            }
        }
        return names;
    }
}
