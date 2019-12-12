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
import com.powsybl.afs.ws.storage.RemoteTaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(AppFileSystemProvider.class)
public class RemoteAppFileSystemProvider implements AppFileSystemProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAppFileSystemProvider.class);

    private final Supplier<Optional<RemoteServiceConfig>> configSupplier;

    public RemoteAppFileSystemProvider() {
        this(RemoteServiceConfig::load);
    }

    public RemoteAppFileSystemProvider(Supplier<Optional<RemoteServiceConfig>> configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
    }

    @Override
    public List<AppFileSystem> getFileSystems(AppFileSystemProviderContext context) {
        Objects.requireNonNull(context);
        Optional<RemoteServiceConfig> config = configSupplier.get();
        if (config.isPresent()) {
            URI uri = config.get().getRestUri();
            try {
                return RemoteAppStorage.getFileSystemNames(uri, context.getToken()).stream()
                        .map(fileSystemName -> {
                            RemoteAppStorage storage = new RemoteAppStorage(fileSystemName, uri, context.getToken());
                            RemoteTaskMonitor taskMonitor = new RemoteTaskMonitor(fileSystemName, uri, context.getToken());
                            return new AppFileSystem(fileSystemName, true, storage, taskMonitor);
                        })
                        .collect(Collectors.toList());
            } catch (ProcessingException e) {
                LOGGER.warn(e.toString());
                return Collections.emptyList();
            }
        } else {
            LOGGER.warn("Remote service config is missing");
            return Collections.emptyList();
        }
    }
}
