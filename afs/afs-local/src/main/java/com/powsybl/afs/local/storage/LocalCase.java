/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.google.common.collect.ImmutableMap;
import com.powsybl.afs.ext.base.Case;
import com.powsybl.afs.storage.AppStorageDataSource;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.math.timeseries.DoubleTimeSeries;
import com.powsybl.math.timeseries.StringTimeSeries;
import com.powsybl.math.timeseries.TimeSeriesMetadata;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

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
    public Path getParentPath() {
        return file.getParent();
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
    public Map<String, String> getStringMetadata() {
        return ImmutableMap.of("format", importer.getFormat());
    }

    @Override
    public Map<String, Double> getDoubleMetadata() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Integer> getIntMetadata() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Boolean> getBooleanMetadata() {
        return Collections.emptyMap();
    }

    @Override
    public Reader readStringData(String name) {
        throw new AssertionError();
    }

    @Override
    public InputStream readBinaryData(String name) {
        DataSource dataSource = Importers.createDataSource(file);
        AppStorageDataSource.Name key = AppStorageDataSource.Name.parse(name);
        if (key instanceof AppStorageDataSource.SuffixAndExtension) {
            try {
                return dataSource.newInputStream(((AppStorageDataSource.SuffixAndExtension) key).getSuffix(),
                                                 ((AppStorageDataSource.SuffixAndExtension) key).getExt());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else if (key instanceof AppStorageDataSource.FileName) {
            try {
                return dataSource.newInputStream(((AppStorageDataSource.FileName) key).getName());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public boolean dataExists(String name) {
        DataSource dataSource = Importers.createDataSource(file);
        AppStorageDataSource.Name key = AppStorageDataSource.Name.parse(name);
        if (key instanceof AppStorageDataSource.SuffixAndExtension) {
            try {
                return dataSource.exists(((AppStorageDataSource.SuffixAndExtension) key).getSuffix(),
                                         ((AppStorageDataSource.SuffixAndExtension) key).getExt());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else if (key instanceof AppStorageDataSource.FileName) {
            try {
                return dataSource.exists(((AppStorageDataSource.FileName) key).getName());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            throw new AssertionError();
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
