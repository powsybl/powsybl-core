/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.ProjectFileBuilder;
import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.iidm.import_.ImportersLoader;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCaseBuilder implements ProjectFileBuilder<ImportedCase> {

    private final NodeInfo folderInfo;

    private final ListenableAppStorage storage;

    private final NodeInfo projectInfo;

    private final AppFileSystem fileSystem;

    private final ImportersLoader importersLoader;

    private Case aCase;

    private final Properties parameters = new Properties();;

    public ImportedCaseBuilder(NodeInfo folderInfo, ListenableAppStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem,
                               ImportersLoader importersLoader) {
        this.folderInfo = Objects.requireNonNull(folderInfo);
        this.storage = Objects.requireNonNull(storage);
        this.projectInfo = Objects.requireNonNull(projectInfo);
        this.fileSystem = Objects.requireNonNull(fileSystem);
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    public ImportedCaseBuilder withCase(Case aCase) {
        this.aCase = Objects.requireNonNull(aCase);
        return this;
    }

    public ImportedCaseBuilder withParameter(String name, String value) {
        parameters.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
        return this;
    }

    public ImportedCaseBuilder withParameters(Map<String, String> parameters) {
        this.parameters.putAll(Objects.requireNonNull(parameters));
        return this;
    }

    @Override
    public ImportedCase build() {
        if (aCase == null) {
            throw new AfsException("Case is not set");
        }

        String name = aCase.getName();

        if (storage.getChildNode(folderInfo.getId(), name) != null) {
            throw new AfsException("Parent folder already contains a '" + name + "' node");
        }

        // create project file
        NodeId id = storage.createNode(folderInfo.getId(), name, ImportedCase.PSEUDO_CLASS);

        // store importer format
        storage.setStringAttribute(id, ImportedCase.FORMAT, aCase.getImporter().getFormat());

        // store case data
        aCase.getImporter().copy(aCase.getDataSource(), storage.getDataSourceAttribute(id, ImportedCase.DATA_SOURCE));

        // store parameters
        try {
            StringWriter writer = new StringWriter();
            try {
                parameters.store(writer, "");
            } finally {
                writer.close();
            }
            storage.setStringAttribute(id, ImportedCase.PARAMETERS, writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        storage.flush();

        return new ImportedCase(new NodeInfo(id, name, ImportedCase.PSEUDO_CLASS), storage, projectInfo, fileSystem, importersLoader);
    }
}
