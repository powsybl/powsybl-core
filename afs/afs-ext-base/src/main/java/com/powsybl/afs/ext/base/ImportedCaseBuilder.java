/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.collect.ImmutableMap;
import com.powsybl.afs.AfsException;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileBuilder;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.storage.AppStorageDataSource;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.iidm.import_.ImportersLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCaseBuilder implements ProjectFileBuilder<ImportedCase> {

    private final ProjectFileBuildContext context;

    private final ImportersLoader importersLoader;

    private Case aCase;

    private final Properties parameters = new Properties();;

    public ImportedCaseBuilder(ProjectFileBuildContext context, ImportersLoader importersLoader) {
        this.context = Objects.requireNonNull(context);
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

        if (context.getStorage().getChildNode(context.getFolderInfo().getId(), name) != null) {
            throw new AfsException("Parent folder already contains a '" + name + "' node");
        }

        // create project file
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, ImportedCase.PSEUDO_CLASS, "", ImportedCase.VERSION,
                ImmutableMap.of(ImportedCase.FORMAT, aCase.getImporter().getFormat()), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

        // store case data
        aCase.getImporter().copy(aCase.getDataSource(), new AppStorageDataSource(context.getStorage(), info.getId()));

        // store parameters
        try (Writer writer = context.getStorage().writeStringData(info.getId(), ImportedCase.PARAMETERS)) {
            parameters.store(writer, "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context.getStorage().flush();

        return new ImportedCase(new ProjectFileCreationContext(info, context.getStorage(), context.getFileSystem()),
                                importersLoader);
    }
}
