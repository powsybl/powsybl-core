/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCase extends ProjectFile implements ProjectCase {

    public static final String PSEUDO_CLASS = "importedCase";

    static final String FORMAT = "format";
    static final String DATA_SOURCE = "dataSource";
    static final String PARAMETERS = "parameters";

    private final ImportersLoader importersLoader;

    public ImportedCase(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem,
                        ImportersLoader importersLoader) {
        super(id, storage, projectId, fileSystem, CaseIconCache.INSTANCE.get(
                importersLoader,
                fileSystem.getData().getComputationManager(),
                getFormat(storage, id)));
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    private static String getFormat(AppFileSystemStorage storage, NodeId id) {
        return storage.getStringAttribute(id, FORMAT);
    }

    public ReadOnlyDataSource getDataSource() {
        return storage.getDataSourceAttribute(id, DATA_SOURCE);
    }

    public Properties getParameters() {
        Properties parameters = new Properties();
        try (StringReader reader = new StringReader(storage.getStringAttribute(id, PARAMETERS))) {
            parameters.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return parameters;
    }

    public Importer getImporter() {
        String format = getFormat(storage, id);
        return importersLoader.loadImporters()
                .stream()
                .filter(importer -> importer.getFormat().equals(format))
                .findFirst()
                .orElseThrow(() -> new AfsException("Importer not found for format " + format));
    }

    @Override
    public Network loadNetwork() {
        Importer importer = getImporter();
        ReadOnlyDataSource dataSource = getDataSource();
        Properties parameters = getParameters();
        return importer.importData(dataSource, parameters);
    }
}
