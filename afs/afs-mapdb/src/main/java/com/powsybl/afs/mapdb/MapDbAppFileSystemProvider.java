/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb;

import com.google.auto.service.AutoService;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.AppFileSystemProvider;
import com.powsybl.afs.AppFileSystemProviderContext;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(AppFileSystemProvider.class)
public class MapDbAppFileSystemProvider implements AppFileSystemProvider {

    private final List<MapDbAppFileSystemConfig> configs;

    private final BiFunction<String, Path, MapDbAppStorage> storageProvider;

    public MapDbAppFileSystemProvider() {
        this(MapDbAppFileSystemConfig.load(), (name, path) -> MapDbAppStorage.createMmapFile(name, path.toFile()));
    }

    public MapDbAppFileSystemProvider(List<MapDbAppFileSystemConfig> configs,
                                      BiFunction<String, Path, MapDbAppStorage> storageProvider) {
        this.configs = Objects.requireNonNull(configs);
        this.storageProvider = Objects.requireNonNull(storageProvider);
    }

    @Override
    public List<AppFileSystem> getFileSystems(AppFileSystemProviderContext context) {
        return configs.stream()
                .map(config ->  {
                    MapDbAppStorage storage = storageProvider.apply(config.getDriveName(), config.getDbFile());
                    return new MapDbAppFileSystem(config.getDriveName(), config.isRemotelyAccessible(), storage);
                })
                .collect(Collectors.toList());
    }
}
