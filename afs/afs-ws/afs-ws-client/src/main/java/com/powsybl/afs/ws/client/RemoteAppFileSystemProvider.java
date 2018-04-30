/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client;

import com.google.auto.service.AutoService;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.AppFileSystemProvider;
import com.powsybl.computation.ComputationManager;
import com.powsybl.afs.ws.storage.RemoteTaskMonitor;
import com.powsybl.afs.ws.storage.RemoteAppStorage;
import com.powsybl.afs.ws.storage.RemoteListenableAppStorage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(AppFileSystemProvider.class)
public class RemoteAppFileSystemProvider implements AppFileSystemProvider {

    private final List<RemoteAppFileSystemConfig> configs;

    public RemoteAppFileSystemProvider() {
        this.configs = RemoteAppFileSystemConfig.load();
    }

    @Override
    public List<AppFileSystem> getFileSystems(ComputationManager computationManager) {
        return configs.stream()
                .map(config ->  {
                    RemoteAppStorage storage = new RemoteAppStorage(config.getFileSystemName(), config.getUrl());
                    RemoteListenableAppStorage listenableStorage = new RemoteListenableAppStorage(storage, config.getUrl());
                    RemoteTaskMonitor taskMonitor = new RemoteTaskMonitor(config.getFileSystemName(), config.getUrl());
                    return new AppFileSystem(config.getFileSystemName(), true, listenableStorage, taskMonitor);
                })
                .collect(Collectors.toList());
    }
}
