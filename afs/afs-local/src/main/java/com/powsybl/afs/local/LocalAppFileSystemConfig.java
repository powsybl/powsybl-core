/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.storage.AbstractAppFileSystemConfig;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppFileSystemConfig extends AbstractAppFileSystemConfig<LocalAppFileSystemConfig> {

    private Path rootDir;

    public static List<LocalAppFileSystemConfig> load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static List<LocalAppFileSystemConfig> load(PlatformConfig platformConfig) {
        List<LocalAppFileSystemConfig> configs = new ArrayList<>();
        ModuleConfig moduleConfig = platformConfig.getModuleConfigIfExists("local-app-file-system");
        if (moduleConfig != null) {
            if (moduleConfig.hasProperty("drive-name")
                    && moduleConfig.hasProperty("root-dir")) {
                String driveName = moduleConfig.getStringProperty("drive-name");
                boolean remotelyAccessible = moduleConfig.getBooleanProperty("remotely-accessible", DEFAULT_REMOTELY_ACCESSIBLE);
                Path rootDir = moduleConfig.getPathProperty("root-dir");
                configs.add(new LocalAppFileSystemConfig(driveName, remotelyAccessible, rootDir));
            }
            int maxAdditionalDriveCount = moduleConfig.getIntProperty("max-additional-drive-count", 0);
            for (int i = 0; i < maxAdditionalDriveCount; i++) {
                if (moduleConfig.hasProperty("drive-name-" + i)
                        && moduleConfig.hasProperty("root-dir-" + i)) {
                    String driveName = moduleConfig.getStringProperty("drive-name-" + i);
                    boolean remotelyAccessible = moduleConfig.getBooleanProperty("remotely-accessible-" + i, DEFAULT_REMOTELY_ACCESSIBLE);
                    Path rootDir = moduleConfig.getPathProperty("root-dir-" + i);
                    configs.add(new LocalAppFileSystemConfig(driveName, remotelyAccessible, rootDir));
                }
            }
        } else {
            for (Path rootDir : platformConfig.getFileSystem().getRootDirectories()) {
                configs.add(new LocalAppFileSystemConfig(rootDir.toString(), false, rootDir));
            }
        }
        return configs;
    }

    private static Path checkRootDir(Path rootDir) {
        Objects.requireNonNull(rootDir);
        if (!Files.isDirectory(rootDir)) {
            throw new AfsException("Root path " + rootDir + " is not a directory");
        }
        return rootDir;
    }

    public LocalAppFileSystemConfig(String driveName, boolean remotelyAccessible, Path rootDir) {
        super(driveName, remotelyAccessible);
        this.rootDir = checkRootDir(rootDir).toAbsolutePath();
    }

    public Path getRootDir() {
        return rootDir;
    }

    public LocalAppFileSystemConfig setRootDir(Path rootDir) {
        this.rootDir = checkRootDir(rootDir);
        return this;
    }
}
