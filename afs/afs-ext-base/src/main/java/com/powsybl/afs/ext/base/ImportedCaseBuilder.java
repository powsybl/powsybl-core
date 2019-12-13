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
import com.powsybl.afs.ext.base.events.CaseImported;
import com.powsybl.afs.storage.*;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.export.ExportersLoader;
import com.powsybl.iidm.export.ExportersServiceLoader;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Builder for the project file {@link ImportedCase}.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportedCaseBuilder implements ProjectFileBuilder<ImportedCase> {

    private final ProjectFileBuildContext context;

    private final ExportersLoader exportersLoader;

    private final ImportersLoader importersLoader;

    private final ImportConfig importConfig;

    private String name;

    private ReadOnlyDataSource dataSource;

    private Importer importer;

    private final Properties parameters = new Properties();

    public ImportedCaseBuilder(ProjectFileBuildContext context, ImportersLoader importersLoader, ImportConfig importConfig) {
        this(context, new ExportersServiceLoader(), importersLoader, importConfig);
    }

    public ImportedCaseBuilder(ProjectFileBuildContext context, ExportersLoader exportersLoader, ImportersLoader importersLoader, ImportConfig importConfig) {
        this.context = Objects.requireNonNull(context);
        this.exportersLoader = Objects.requireNonNull(exportersLoader);
        this.importersLoader = Objects.requireNonNull(importersLoader);
        this.importConfig = Objects.requireNonNull(importConfig);
    }

    public ImportedCaseBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ImportedCaseBuilder withCase(Case aCase) {
        Objects.requireNonNull(aCase);
        if (name == null) {
            name = aCase.getName();
        }
        dataSource = aCase.getDataSource();
        importer = aCase.getImporter();
        return this;
    }

    public ImportedCaseBuilder withFile(Path file) {
        withDatasource(Importers.createDataSource(file));
        if (name == null) {
            name = DataSourceUtil.getBaseName(file);
        }
        return this;
    }

    public ImportedCaseBuilder withDatasource(ReadOnlyDataSource dataSource) {
        Objects.requireNonNull(dataSource);
        importer = Importers.findImporter(dataSource, importersLoader, context.getProject().getFileSystem().getData().getShortTimeExecutionComputationManager(), importConfig);
        if (importer == null) {
            throw new AfsException("No importer found for this data source");
        }
        this.dataSource = dataSource;
        return this;
    }

    public ImportedCaseBuilder withNetwork(Network network) {
        Objects.requireNonNull(network);
        if (name == null) {
            name = network.getId();
        }
        DataSource memDataSource = new MemDataSource();
        Exporters.export(exportersLoader, "XIIDM", network, null, memDataSource);
        return withDatasource(memDataSource);
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
        if (dataSource == null) {
            throw new AfsException("Case or data source is not set");
        }
        if (name == null) {
            throw new AfsException("Name is not set (mandatory when importing directly from a data source)");
        }

        if (context.getStorage().getChildNode(context.getFolderInfo().getId(), name).isPresent()) {
            throw new AfsException("Parent folder already contains a '" + name + "' node");
        }

        // create project file
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, ImportedCase.PSEUDO_CLASS, "", ImportedCase.VERSION,
                new NodeGenericMetadata().setString(ImportedCase.FORMAT, importer.getFormat()));

        // store case data
        importer.copy(dataSource, new AppStorageDataSource(context.getStorage(), info.getId(), info.getName()));

        // store parameters
        try (Writer writer = new OutputStreamWriter(context.getStorage().writeBinaryData(info.getId(), ImportedCase.PARAMETERS), StandardCharsets.UTF_8)) {
            parameters.store(writer, "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        context.getStorage().setConsistent(info.getId());

        context.getStorage().flush();

        ImportedCase ic = new ImportedCase(new ProjectFileCreationContext(info, context.getStorage(), context.getProject()),
                                importersLoader);

        context.getStorage().getEventsBus().pushEvent(new CaseImported(info.getId(),
                context.getFolderInfo().getId(), ic.getPath().toString()), CaseImported.TYPE);

        return ic;
    }
}
