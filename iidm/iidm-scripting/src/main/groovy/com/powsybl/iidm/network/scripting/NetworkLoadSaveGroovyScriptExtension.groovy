/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting

import com.google.auto.service.AutoService
import com.powsybl.computation.ComputationManager
import com.powsybl.computation.local.LocalComputationManager
import com.powsybl.iidm.export.Exporters
import com.powsybl.iidm.export.ExportersLoader
import com.powsybl.iidm.export.ExportersServiceLoader
import com.powsybl.iidm.import_.ImportConfig
import com.powsybl.iidm.import_.Importers
import com.powsybl.iidm.import_.ImportersLoader
import com.powsybl.iidm.import_.ImportersServiceLoader
import com.powsybl.iidm.network.Network
import com.powsybl.scripting.groovy.GroovyScriptExtension

import java.nio.file.FileSystem
import java.nio.file.FileSystems

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(GroovyScriptExtension.class)
class NetworkLoadSaveGroovyScriptExtension implements GroovyScriptExtension {

    private final ImportConfig importConfig

    private final ImportersLoader importersLoader

    private final ExportersLoader exportersLoader

    private final FileSystem fileSystem

    NetworkLoadSaveGroovyScriptExtension() {
        this(ImportConfig.load(), new ImportersServiceLoader(), new ExportersServiceLoader(), FileSystems.getDefault())
    }

    NetworkLoadSaveGroovyScriptExtension(ImportConfig importConfig, ImportersLoader importersLoader,
                                         ExportersLoader exportersLoader, FileSystem fileSystem) {
        this.importConfig = Objects.requireNonNull(importConfig)
        this.importersLoader = Objects.requireNonNull(importersLoader)
        this.exportersLoader = Objects.requireNonNull(exportersLoader)
        this.fileSystem = Objects.requireNonNull(fileSystem)
    }

    @Override
    void load(Binding binding, ComputationManager computationManager) {
        binding.loadNetwork = { String file, Properties parameters = null ->
            Importers.loadNetwork(fileSystem.getPath(file), LocalComputationManager.getDefault(),
                    importConfig, parameters, importersLoader)
        }
        binding.saveNetwork =  { String format, Network network, Properties parameters = null, String file ->
            Exporters.export(exportersLoader, format, network, parameters, fileSystem.getPath(file))
        }
    }

    @Override
    void unload() {
    }
}
