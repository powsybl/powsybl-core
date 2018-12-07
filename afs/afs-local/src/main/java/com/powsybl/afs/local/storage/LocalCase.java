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
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.timeseries.DoubleDataChunk;
import com.powsybl.timeseries.StringDataChunk;
import com.powsybl.timeseries.TimeSeriesMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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
        return DataSourceUtil.getBaseName(file);
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
        return AppStorageDataSource.Name.parse(name, new AppStorageDataSource.NameHandler<Optional<InputStream>>() {
            @Override
            public Optional<InputStream> onSuffixAndExtension(AppStorageDataSource.SuffixAndExtension suffixAndExtension) throws IOException {
                return Optional.of(dataSource.newInputStream(suffixAndExtension.getSuffix(), suffixAndExtension.getExt()));
            }

            @Override
            public Optional<InputStream> onFileName(AppStorageDataSource.FileName fileName) throws IOException {
                return Optional.of(dataSource.newInputStream(fileName.getName()));
            }

            @Override
            public Optional<InputStream> onOther(AppStorageDataSource.Name name) {
                throw new AssertionError("Unknown data source name " + name);
            }
        });
    }

    @Override
    public boolean dataExists(String name) {
        DataSource dataSource = Importers.createDataSource(file);
        return AppStorageDataSource.Name.parse(name, new AppStorageDataSource.NameHandler<Boolean>() {
            @Override
            public Boolean onSuffixAndExtension(AppStorageDataSource.SuffixAndExtension suffixAndExtension) throws IOException {
                return dataSource.exists(suffixAndExtension.getSuffix(), suffixAndExtension.getExt());
            }

            @Override
            public Boolean onFileName(AppStorageDataSource.FileName fileName) throws IOException {
                return dataSource.exists(fileName.getName());
            }

            @Override
            public Boolean onOther(AppStorageDataSource.Name name) {
                throw new AssertionError("Unknown data source name " + name);
            }
        });
    }

    @Override
    public Set<String> getDataNames() {
        DataSource dataSource = Importers.createDataSource(file);
        try {
            Set<String> names = dataSource.listNames(".*");
            LOG.info("LocalCase::getDataNames()");
            names.forEach(n -> LOG.info("    {}", n));
            return names;
        } catch (IOException x) {
            throw new UncheckedIOException("getDataNames()", x);
        }
    }

    @Override
    public Set<String> getTimeSeriesNames() {
        throw new AssertionError();
    }

    @Override
    public boolean timeSeriesExists(String timeSeriesName) {
        throw new AssertionError();
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions() {
        throw new AssertionError();
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String timeSeriesName) {
        throw new AssertionError();
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
        throw new AssertionError();
    }

    @Override
    public Map<String, List<DoubleDataChunk>> getDoubleTimeSeriesData(Set<String> timeSeriesNames, int version) {
        throw new AssertionError();
    }

    @Override
    public Map<String, List<StringDataChunk>> getStringTimeSeriesData(Set<String> timeSeriesNames, int version) {
        throw new AssertionError();
    }

    private static final Logger LOG = LoggerFactory.getLogger(LocalCase.class);
}
