/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client;

import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.AppFileSystemProvider;
import com.powsybl.afs.AppFileSystemProviderContext;
import com.powsybl.afs.ws.client.utils.RemoteServiceConfig;
import com.powsybl.afs.ws.storage.RemoteAppStorage;
import com.powsybl.afs.ws.storage.RemoteListenableAppStorage;
import com.powsybl.afs.ws.storage.RemoteTaskMonitor;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(AppFileSystemProvider.class)
public class RemoteAppFileSystemProvider implements AppFileSystemProvider {

    private final Supplier<RemoteServiceConfig> configSupplier;

    public RemoteAppFileSystemProvider() {
        this(RemoteServiceConfig.INSTANCE);
    }

    public RemoteAppFileSystemProvider(Supplier<RemoteServiceConfig> configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
    }

    @Override
    public List<AppFileSystem> getFileSystems(AppFileSystemProviderContext context) {
        Objects.requireNonNull(context);
        if (context.getToken() == null) {
            return Collections.emptyList();
        } else {
            RemoteServiceConfig config = configSupplier.get();
            Objects.requireNonNull(config);
            URI uri = config.getRestUri();
            return RemoteAppStorage.getFileSystemNames(uri, context.getToken()).stream()
                    .map(fileSystemName -> {
                        RemoteAppStorage storage = new RemoteAppStorage(fileSystemName, uri, context.getToken());
                        RemoteListenableAppStorage listenableStorage = new RemoteListenableAppStorage(storage, uri);
                        RemoteTaskMonitor taskMonitor = new RemoteTaskMonitor(fileSystemName, uri, context.getToken());
                        return new AppFileSystem(fileSystemName, true, listenableStorage, taskMonitor);
                    })
                    .collect(Collectors.toList());
        }
    }
}
