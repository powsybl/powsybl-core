/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.import_

import com.google.auto.service.AutoService
import eu.itesla_project.computation.ComputationManager
import eu.itesla_project.computation.script.GroovyExtension

import java.nio.file.FileSystem
import java.nio.file.FileSystems

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(GroovyExtension.class)
class ImportGroovyExtension implements GroovyExtension {

    private final FileSystem fileSystem;

    private final ImportersLoader importersLoader

    ImportGroovyExtension(FileSystem fileSystem, ImportersLoader importersLoader) {
        assert fileSystem;
        assert importersLoader;
        this.fileSystem = fileSystem
        this.importersLoader = importersLoader;
    }

    ImportGroovyExtension() {
        this(FileSystems.getDefault(), new ImportersServiceLoader())
    }

    @Override
    void load(Binding binding, ComputationManager computationManager) {
        binding.loadNetwork = { file ->
            Importers.loadNetwork(fileSystem.getPath(file), computationManager, new ImportConfig(), null, importersLoader)
        }
    }
}
