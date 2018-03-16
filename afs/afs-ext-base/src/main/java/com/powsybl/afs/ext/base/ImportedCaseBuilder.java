/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileBuilder;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.storage.AppStorageDataSource;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.iidm.import_.ImportersLoader;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCaseBuilder implements ProjectFileBuilder<ImportedCase> {

    private final ProjectFileBuildContext context;

    private final ImportersLoader importersLoader;

    private String name;

    private Case aCase;

    private final Properties parameters = new Properties();

    public ImportedCaseBuilder(ProjectFileBuildContext context, ImportersLoader importersLoader) {
        this.context = Objects.requireNonNull(context);
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    public ImportedCaseBuilder withName(String name) {
        this.name = name;
        return this;
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

        String importedCaseName = this.name != null ? this.name : aCase.getName();

        if (context.getStorage().getChildNode(context.getFolderInfo().getId(), importedCaseName).isPresent()) {
            throw new AfsException("Parent folder already contains a '" + importedCaseName + "' node");
        }

        // create project file
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), importedCaseName, ImportedCase.PSEUDO_CLASS, "", ImportedCase.VERSION,
                new NodeGenericMetadata().setString(ImportedCase.FORMAT, aCase.getImporter().getFormat()));

        // store case data
        aCase.getImporter().copy(aCase.getDataSource(), new AppStorageDataSource(context.getStorage(), info.getId()));

        // store parameters
        try (Writer writer = new OutputStreamWriter(context.getStorage().writeBinaryData(info.getId(), ImportedCase.PARAMETERS), StandardCharsets.UTF_8)) {
            parameters.store(writer, "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context.getStorage().flush();

        return new ImportedCase(new ProjectFileCreationContext(info, context.getStorage(), context.getFileSystem()),
                                importersLoader);
    }
}
