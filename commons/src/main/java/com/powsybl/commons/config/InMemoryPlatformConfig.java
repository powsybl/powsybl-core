/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InMemoryPlatformConfig extends PlatformConfig {

    protected static class InMemoryModuleConfigContainer implements ModuleConfigContainer {

        private final Map<String, MapModuleConfig> configs = new HashMap<>();

        private final FileSystem fileSystem;

        protected InMemoryModuleConfigContainer(FileSystem fileSystem) {
            this.fileSystem = Objects.requireNonNull(fileSystem);
        }

        protected Map<String, MapModuleConfig> getConfigs() {
            return configs;
        }

        @Override
        public boolean moduleExists(String name) {
            return configs.containsKey(name);
        }

        @Override
        public ModuleConfig getModuleConfig(String name) {
            ModuleConfig config = configs.get(name);
            if (config == null) {
                throw new PowsyblException("Module " + name + " not found");
            }
            return config;
        }

        @Override
        public ModuleConfig getModuleConfigIfExists(String name) {
            return configs.get(name);
        }

        public MapModuleConfig createModuleConfig(String name) {
            MapModuleConfig config = configs.get(name);
            if (config != null) {
                throw new PowsyblException("Module " + name + " already exists");
            }
            config = new MapModuleConfig(fileSystem);
            configs.put(name, config);
            return config;
        }
    }

    public InMemoryPlatformConfig(FileSystem fileSystem) {
        super(fileSystem, new InMemoryModuleConfigContainer(fileSystem));
    }

    public InMemoryPlatformConfig(FileSystem fileSystem, Path configDir, Path cacheDir) {
        super(fileSystem, configDir, cacheDir, new InMemoryModuleConfigContainer(fileSystem));
    }

    public MapModuleConfig createModuleConfig(String name) {
        return ((InMemoryModuleConfigContainer) container).createModuleConfig(name);
    }
}
