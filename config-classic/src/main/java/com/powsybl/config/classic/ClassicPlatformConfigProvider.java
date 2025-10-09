/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.config.classic;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.powsybl.commons.config.ModuleConfigRepository;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigProvider;
import com.powsybl.commons.config.PlatformEnv;
import com.powsybl.commons.config.EnvironmentModuleConfigRepository;
import com.powsybl.commons.config.StackedModuleConfigRepository;

import com.google.auto.service.AutoService;

/**
 * The classic powsybl PlatformConfig provider. It uses System Properties to
 * get config dirs (powsybl.config.dirs, itools.config.dir; defaults to
 * $HOME/.itools) and reads configuration from yaml, xml or java properties
 * files. The config dir names can use the keywords from {@link PlatformEnv}
 * (e.g. app.root, user.home). It also uses
 * {@link EnvironmentModuleConfigRepository} to read configuration from
 * environment variables.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
@AutoService(PlatformConfigProvider.class)
public class ClassicPlatformConfigProvider implements PlatformConfigProvider {

    private static final String NAME = "classic";

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns the list of default config directories: they are defined by the system properties
     * "powsybl.config.dirs" or "itools.config.dir".
     * If none is defined, it defaults to the single directory ${HOME}/.itools.
     */
    public static Path[] getDefaultConfigDirs(FileSystem fileSystem, String directories, String userHome, String pathSeparator) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(userHome);
        Path[] configDirs = null;
        if (directories != null && !directories.isEmpty()) {
            configDirs = Arrays.stream(directories.split(pathSeparator))
                    .map(PlatformEnv::substitute)
                    .map(fileSystem::getPath)
                    .toArray(Path[]::new);
        }
        if (configDirs == null || configDirs.length == 0) {
            configDirs = new Path[] {fileSystem.getPath(userHome, ".itools")};
        }
        return configDirs;
    }

    /**
     * Loads a {@link ModuleConfigRepository} from the list of specified config directories.
     * Configuration properties values encountered first in the list of directories
     * take precedence over the values defined in subsequent directories.
     * Configuration properties encountered in environment variables take precedence
     * over the values defined in config directories.
     */
    static ModuleConfigRepository loadModuleRepository(Path[] configDirs, String configName) {
        List<ModuleConfigRepository> repositoriesFromPath = Arrays.stream(configDirs)
                .map(configDir -> PlatformConfig.loadModuleRepository(configDir, configName))
                .toList();
        List<ModuleConfigRepository> repositories = new ArrayList<>();
        repositories.add(new EnvironmentModuleConfigRepository(System.getenv(), FileSystems.getDefault()));
        repositories.addAll(repositoriesFromPath);
        return new StackedModuleConfigRepository(repositories);
    }

    @Override
    public PlatformConfig getPlatformConfig() {
        FileSystem fileSystem = FileSystems.getDefault();
        String directories = System.getProperty("powsybl.config.dirs", System.getProperty("itools.config.dir"));
        String configName = System.getProperty("powsybl.config.name",
                System.getProperty("itools.config.name", "config"));
        String userHome = System.getProperty("user.home");
        Path[] configDirs = getDefaultConfigDirs(fileSystem, directories, userHome, File.pathSeparator);
        ModuleConfigRepository repository = loadModuleRepository(configDirs, configName);
        return new PlatformConfig(repository, configDirs[0]);
    }

}
