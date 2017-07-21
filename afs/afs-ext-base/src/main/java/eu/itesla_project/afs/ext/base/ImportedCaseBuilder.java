/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.ext.base;

import eu.itesla_project.afs.core.*;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;

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

    private final NodeId folderId;

    private final AppFileSystemStorage storage;

    private final NodeId projectId;

    private final AppFileSystem fileSystem;

    private Case aCase;

    private final Properties parameters = new Properties();;

    public ImportedCaseBuilder(NodeId folderId, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        this.folderId = Objects.requireNonNull(folderId);
        this.storage = Objects.requireNonNull(storage);
        this.projectId = Objects.requireNonNull(projectId);
        this.fileSystem = Objects.requireNonNull(fileSystem);
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

        try {
            // create project file
            NodeId id = storage.createNode(folderId, aCase.getName(), ImportedCase.PSEUDO_CLASS);

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

            storage.commit();

            return new ImportedCase(id, storage, projectId, fileSystem);
        } catch (Exception e) {
            storage.rollback();
            throw e;
        }
    }
}
