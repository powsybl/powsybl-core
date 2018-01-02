/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.powsybl.afs.ext.base.Case;
import com.powsybl.afs.storage.AppStorageDataSource;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.math.timeseries.DoubleTimeSeries;
import com.powsybl.math.timeseries.StringTimeSeries;
import com.powsybl.math.timeseries.TimeSeriesMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalCase implements LocalFile {

    private final Path file;

    private final Importer importer;

    public LocalCase(Path file, Importer importer) {
        this.file = Objects.requireNonNull(file);
        this.importer = Objects.requireNonNull(importer);
    }

    @Override
    public String getName() {
        return file.getFileName().toString();
    }

    @Override
    public Optional<Path> getParentPath() {
        return Optional.ofNullable(file.getParent());
    }

    @Override
    public String getPseudoClass() {
        return Case.PSEUDO_CLASS;
    }

    @Override
    public String getDescription() {
        return importer.getComment();
    }

    @Override
    public NodeGenericMetadata getGenericMetadata() {
        return new NodeGenericMetadata().setString("format", importer.getFormat());
    }

    @Override
    public Optional<InputStream> readBinaryData(String name) {
        DataSource dataSource = Importers.createDataSource(file);
        AppStorageDataSource.Name dataSrcName = AppStorageDataSource.Name.parse(name);
        try {
            if (dataSrcName instanceof AppStorageDataSource.SuffixAndExtension) {
                return Optional.of(dataSource.newInputStream(((AppStorageDataSource.SuffixAndExtension) dataSrcName).getSuffix(),
                                                             ((AppStorageDataSource.SuffixAndExtension) dataSrcName).getExt()));
            } else if (dataSrcName instanceof AppStorageDataSource.FileName) {
                return Optional.of(dataSource.newInputStream(((AppStorageDataSource.FileName) dataSrcName).getName()));
            } else {
                throw new AssertionError("Unknown data source name " + dataSrcName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean dataExists(String name) {
        DataSource dataSource = Importers.createDataSource(file);
        AppStorageDataSource.Name dataSrcName = AppStorageDataSource.Name.parse(name);
        try {
            if (dataSrcName instanceof AppStorageDataSource.SuffixAndExtension) {
                return dataSource.exists(((AppStorageDataSource.SuffixAndExtension) dataSrcName).getSuffix(),
                                         ((AppStorageDataSource.SuffixAndExtension) dataSrcName).getExt());
            } else if (dataSrcName instanceof AppStorageDataSource.FileName) {
                return dataSource.exists(((AppStorageDataSource.FileName) dataSrcName).getName());
            } else {
                throw new AssertionError("Unknown data source name " + dataSrcName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Set<String> getTimeSeriesNames() {
        throw new AssertionError();
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
        throw new AssertionError();
    }

    @Override
    public List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames, int version) {
        throw new AssertionError();
    }

    @Override
    public List<StringTimeSeries> getStringTimeSeries(Set<String> timeSeriesNames, int version) {
        throw new AssertionError();
    }
}
