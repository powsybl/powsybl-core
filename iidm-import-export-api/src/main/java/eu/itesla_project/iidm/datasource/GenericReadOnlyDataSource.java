/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GenericReadOnlyDataSource extends AbstractDataSource {

    private final DataSource[] dataSources;

    public GenericReadOnlyDataSource(Path directory, String baseName, DataSourceObserver observer) {
        dataSources = new DataSource[] { new FileDataSource(directory, baseName, observer),
                                         new GzFileDataSource(directory, baseName, observer),
                                         new ZipFileDataSource(directory, baseName + ".zip", baseName, observer) };
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
        for (DataSource dataSource : dataSources) {
            if (dataSource.exists(suffix, ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        for (DataSource dataSource : dataSources) {
            if (dataSource.exists(fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        for (DataSource dataSource : dataSources) {
            if (dataSource.exists(suffix, ext)) {
                return dataSource.newInputStream(suffix, ext);
            }
        }
        throw new IOException(getFileName(getBaseName(), suffix, ext) + " not found");
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        for (DataSource dataSource : dataSources) {
            if (dataSource.exists(fileName)) {
                return dataSource.newInputStream(fileName);
            }
        }
        throw new IOException(fileName + " not found");
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        throw new UnsupportedOperationException();
    }
}
