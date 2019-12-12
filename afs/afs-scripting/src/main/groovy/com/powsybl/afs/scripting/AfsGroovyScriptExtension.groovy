/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.scripting

import com.google.auto.service.AutoService
import com.powsybl.afs.AppData
import com.powsybl.afs.AppFileSystem
import com.powsybl.afs.AppFileSystemProvider
import com.powsybl.afs.FileExtension
import com.powsybl.afs.ProjectFileExtension
import com.powsybl.afs.ServiceExtension
import com.powsybl.afs.SoutTaskListener
import com.powsybl.commons.util.ServiceLoaderCache
import com.powsybl.computation.ComputationManager
import com.powsybl.computation.DefaultComputationManagerConfig
import com.powsybl.scripting.groovy.GroovyScriptExtension

/**
 * @author Mathieu BAGUE <mathieu.bague at rte-france.com>
 */
@AutoService(GroovyScriptExtension.class)
class AfsGroovyScriptExtension implements GroovyScriptExtension {

    private final AppData data;

    AfsGroovyScriptExtension() {
        this(new ServiceLoaderCache<>(AppFileSystemProvider.class).getServices(),
                new ServiceLoaderCache<>(FileExtension.class).getServices(),
                new ServiceLoaderCache<>(ProjectFileExtension.class).getServices(),
                new ServiceLoaderCache<>(ServiceExtension.class).getServices(),
                DefaultComputationManagerConfig.load());
    }

    AfsGroovyScriptExtension(List<AppFileSystemProvider> fileSystemProviders,
                             List<FileExtension> fileExtensions, List<ProjectFileExtension> projectFileExtensions,
                             List<ServiceExtension> serviceExtensions, DefaultComputationManagerConfig config) {
        assert fileSystemProviders
        assert fileExtensions
        assert projectFileExtensions
        assert serviceExtensions
        assert config

        data = new AppData(config.createShortTimeExecutionComputationManager(),
                config.createLongTimeExecutionComputationManager(),
                fileSystemProviders,
                fileExtensions,
                projectFileExtensions,
                serviceExtensions);
    }

    @Override
    void load(Binding binding, ComputationManager computationManager) {
        binding.afs = new AfsGroovyFacade(data);

        if (binding.hasProperty("out")) {
            SoutTaskListener listener = new SoutTaskListener(binding.out);
            for (AppFileSystem fileSystem : data.getFileSystems()) {
                fileSystem.getTaskMonitor().addListener(listener);
            }
        }
    }

    @Override
    void unload() {
        if (data != null) {
            data.close();
        }
    }
}
