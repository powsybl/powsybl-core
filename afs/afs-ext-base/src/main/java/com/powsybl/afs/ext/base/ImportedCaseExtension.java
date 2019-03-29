/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.auto.service.AutoService;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.ProjectFileExtension;
import com.powsybl.iidm.export.ExportersLoader;
import com.powsybl.iidm.export.ExportersServiceLoader;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.import_.ImportersServiceLoader;

import java.util.Objects;

/**
 * Defines the new type of project file {@link ImportedCase}.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ProjectFileExtension.class)
public class ImportedCaseExtension implements ProjectFileExtension<ImportedCase, ImportedCaseBuilder> {

    private final ExportersLoader exportersLoader;

    private final ImportersLoader importersLoader;

    private final ImportConfig importConfig;

    public ImportedCaseExtension() {
        this(new ImportersServiceLoader(), ImportConfig.load());
    }

    public ImportedCaseExtension(ImportersLoader importersLoader, ImportConfig importConfig) {
        this(new ExportersServiceLoader(), importersLoader, importConfig);
    }

    public ImportedCaseExtension(ExportersLoader exportersLoader, ImportersLoader importersLoader, ImportConfig importConfig) {
        this.exportersLoader = Objects.requireNonNull(exportersLoader);
        this.importersLoader = Objects.requireNonNull(importersLoader);
        this.importConfig = Objects.requireNonNull(importConfig);
    }

    @Override
    public Class<ImportedCase> getProjectFileClass() {
        return ImportedCase.class;
    }

    @Override
    public String getProjectFilePseudoClass() {
        return ImportedCase.PSEUDO_CLASS;
    }

    @Override
    public Class<ImportedCaseBuilder> getProjectFileBuilderClass() {
        return ImportedCaseBuilder.class;
    }

    @Override
    public ImportedCase createProjectFile(ProjectFileCreationContext context) {
        return new ImportedCase(context, importersLoader);
    }

    @Override
    public ImportedCaseBuilder createProjectFileBuilder(ProjectFileBuildContext context) {
        return new ImportedCaseBuilder(context, exportersLoader, importersLoader, importConfig);
    }
}
