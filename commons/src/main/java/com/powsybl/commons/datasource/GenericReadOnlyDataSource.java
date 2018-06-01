/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GenericReadOnlyDataSource implements ReadOnlyDataSource {

    private final ReadOnlyDataSource[] dataSources;

    public GenericReadOnlyDataSource(Path directory, String baseName, DataSourceObserver observer) {
        dataSources = new DataSource[] {
            new FileDataSource(directory, baseName, observer),
            new ZipFileDataSource(directory, baseName + ".zip", baseName, observer),
            new GzFileDataSource(directory, baseName, observer),
            new Bzip2FileDataSource(directory, baseName, observer)
        };
    }

    public GenericReadOnlyDataSource(Path directory, String baseName) {
        this(directory, baseName, null);
    }

    @Override
    public String getBaseName() {
        return dataSources[0].getBaseName();
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
        throw new UnsupportedOperationException("Not implemented");
    }
}
