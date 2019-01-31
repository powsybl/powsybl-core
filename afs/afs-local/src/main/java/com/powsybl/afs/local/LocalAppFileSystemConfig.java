/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.storage.AbstractAppFileSystemConfig;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.ConfigVersion;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

import static com.powsybl.commons.config.ConfigVersion.DEFAULT_CONFIG_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppFileSystemConfig extends AbstractAppFileSystemConfig<LocalAppFileSystemConfig> implements Versionable {

    private static final String CONFIG_MODULE_NAME = "local-app-file-system";

    private ConfigVersion version = new ConfigVersion(DEFAULT_CONFIG_VERSION);

    private Path rootDir;

    public static List<LocalAppFileSystemConfig> load() {
        return load(PlatformConfig.defaultConfig());
    }

    private static void load(ModuleConfig moduleConfig, OptionalInt num, List<LocalAppFileSystemConfig> configs) {
        StringBuilder driveNameTag = new StringBuilder("drive-name");
        StringBuilder rootDirTag = new StringBuilder("root-dir");
        StringBuilder remotelyAccessibleTag = new StringBuilder("remotely-accessible");
        num.ifPresent(value -> {
            driveNameTag.append("-").append(value);
            rootDirTag.append("-").append(value);
            remotelyAccessibleTag.append("-").append(value);
        });
        if (moduleConfig.hasProperty(driveNameTag.toString())
                && moduleConfig.hasProperty(rootDirTag.toString())) {
            String driveName = moduleConfig.getStringProperty(driveNameTag.toString());
            boolean remotelyAccessible = moduleConfig.getBooleanProperty(remotelyAccessibleTag.toString(), DEFAULT_REMOTELY_ACCESSIBLE);
            Path rootDir = moduleConfig.getPathProperty(rootDirTag.toString());
            configs.add(moduleConfig.getOptionalStringProperty("version")
                    .map(v -> new LocalAppFileSystemConfig(new ConfigVersion(v), driveName, remotelyAccessible, rootDir))
                    .orElseGet(() -> new LocalAppFileSystemConfig(driveName, remotelyAccessible, rootDir)));
        }
    }

    public static List<LocalAppFileSystemConfig> load(PlatformConfig platformConfig) {
        return platformConfig.getOptionalModuleConfig(CONFIG_MODULE_NAME)
                .map(moduleConfig -> {
                    List<LocalAppFileSystemConfig> configs = new ArrayList<>();
                    load(moduleConfig, OptionalInt.empty(), configs);
                    int maxAdditionalDriveCount = moduleConfig.getIntProperty("max-additional-drive-count", 0);
                    for (int i = 0; i < maxAdditionalDriveCount; i++) {
                        load(moduleConfig, OptionalInt.of(i), configs);
                    }
                    return configs;
                })
                .orElseGet(() -> {
                    List<LocalAppFileSystemConfig> configs = new ArrayList<>();
                    for (Path rootDir : FileSystems.getDefault().getRootDirectories()) {
                        if (Files.isDirectory(rootDir)) {
                            configs.add(new LocalAppFileSystemConfig(rootDir.toString(), false, rootDir));
                        }
                    }
                    return configs;
                });
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

    public LocalAppFileSystemConfig(ConfigVersion version, String driveName, boolean remotelyAccessible, Path rootDir) {
        this(driveName, remotelyAccessible, rootDir);
        this.version = version;
    }

    public Path getRootDir() {
        return rootDir;
    }

    public LocalAppFileSystemConfig setRootDir(Path rootDir) {
        this.rootDir = checkRootDir(rootDir);
        return this;
    }

    @Override
    public String getName() {
        return CONFIG_MODULE_NAME;
    }

    @Override
    public String getVersion() {
        return version.toString();
    }
}
