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
import com.powsybl.timeseries.DoubleArrayChunk;
import com.powsybl.timeseries.StringArrayChunk;
import com.powsybl.timeseries.TimeSeriesMetadata;

import java.io.InputStream;
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
        DataSource dataSource = Importers.createDataSource(file);
        return importer.getPrettyName(dataSource);
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
        DataSource dataSource = Importers.createDataSource(file);
        String mainFileName = dataSource.getMainFileName();
        NodeGenericMetadata metadata = new NodeGenericMetadata()
                .setString("format", importer.getFormat());
        if (mainFileName != null) {
            metadata.setString(AppStorageDataSource.MAIN_FILE_NAME, mainFileName);
        }
        return metadata;
    }

    @Override
    public Optional<InputStream> readBinaryData(String name) {
        DataSource dataSource = Importers.createDataSource(file);
        return Optional.of(dataSource.newInputStream(name));
    }

    @Override
    public boolean dataExists(String name) {
        DataSource dataSource = Importers.createDataSource(file);
        return dataSource.fileExists(name);
    }

    @Override
    public Set<String> getDataNames() {
        DataSource dataSource = Importers.createDataSource(file);
        return dataSource.getFileNames(".*");
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
    public Map<String, List<DoubleArrayChunk>> getDoubleTimeSeriesData(Set<String> timeSeriesNames, int version) {
        throw new AssertionError();
    }

    @Override
    public Map<String, List<StringArrayChunk>> getStringTimeSeriesData(Set<String> timeSeriesNames, int version) {
        throw new AssertionError();
    }
}
