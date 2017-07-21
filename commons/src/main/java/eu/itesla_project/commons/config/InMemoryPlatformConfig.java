/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.config;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InMemoryPlatformConfig extends PlatformConfig {

    protected final Map<String, MapModuleConfig> configs = new HashMap<>();

    public InMemoryPlatformConfig(FileSystem fs) {
        super(fs);
    }

    public InMemoryPlatformConfig(FileSystem fs, Path configDir, Path cacheDir) {
        super(fs, configDir, cacheDir);
    }

    @Override
    public boolean moduleExists(String name) {
        return configs.containsKey(name);
    }

    @Override
    public ModuleConfig getModuleConfig(String name) {
        ModuleConfig config = configs.get(name);
        if (config == null) {
            throw new RuntimeException("Module " + name + " not found");
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
            throw new RuntimeException("Module " + name + " already exists");
        }
        config = new MapModuleConfig(fileSystem);
        configs.put(name, config);
        return config;
    }
}
