/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.storage.AbstractAppFileSystemConfig;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.ConfigVersion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbAppFileSystemConfig extends AbstractAppFileSystemConfig<MapDbAppFileSystemConfig> implements Versionable {

    private static final String CONFIG_MODULE_NAME = "mapdb-app-file-system";

    static final String DEFAULT_CONFIG_VERSION = "1.0";

    private ConfigVersion version = new ConfigVersion(DEFAULT_CONFIG_VERSION);

    private Path dbFile;

    public static List<MapDbAppFileSystemConfig> load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static List<MapDbAppFileSystemConfig> load(PlatformConfig platformConfig) {

        return platformConfig.getOptionalModuleConfig(CONFIG_MODULE_NAME)
                .map(moduleConfig -> {
                    List<MapDbAppFileSystemConfig> configs = new ArrayList<>();
                    if (moduleConfig.hasProperty("drive-name")
                            && moduleConfig.hasProperty("db-file")) {
                        String driveName = moduleConfig.getStringProperty("drive-name");
                        boolean remotelyAccessible = moduleConfig.getBooleanProperty("remotely-accessible", DEFAULT_REMOTELY_ACCESSIBLE);
                        Path rootDir = moduleConfig.getPathProperty("db-file");
                        configs.add(moduleConfig.getOptionalStringProperty("version")
                                .map(v -> new MapDbAppFileSystemConfig(new ConfigVersion(v), driveName, remotelyAccessible, rootDir))
                                .orElseGet(() -> new MapDbAppFileSystemConfig(driveName, remotelyAccessible, rootDir)));
                    }
                    int maxAdditionalDriveCount = moduleConfig.getIntProperty("max-additional-drive-count", 0);
                    for (int i = 0; i < maxAdditionalDriveCount; i++) {
                        if (moduleConfig.hasProperty("drive-name-" + i)
                                && moduleConfig.hasProperty("db-file-" + i)) {
                            String driveName = moduleConfig.getStringProperty("drive-name-" + i);
                            boolean remotelyAccessible = moduleConfig.getBooleanProperty("remotely-accessible-" + i, DEFAULT_REMOTELY_ACCESSIBLE);
                            Path rootDir = moduleConfig.getPathProperty("db-file-" + i);
                            configs.add(moduleConfig.getOptionalStringProperty("version")
                                    .map(v -> new MapDbAppFileSystemConfig(new ConfigVersion(v), driveName, remotelyAccessible, rootDir))
                                    .orElseGet(() -> new MapDbAppFileSystemConfig(driveName, remotelyAccessible, rootDir)));
                        }
                    }
                    return configs;
                }).orElseGet(ArrayList::new);
    }

    private static Path checkDbFile(Path dbFile) {
        Objects.requireNonNull(dbFile);
        if (Files.isDirectory(dbFile)) {
            throw new AfsException("DB file " + dbFile + " is a directory");
        }
        return dbFile;
    }

    public MapDbAppFileSystemConfig(String driveName, boolean remotelyAccessible, Path dbFile) {
        super(driveName, remotelyAccessible);
        this.dbFile = checkDbFile(dbFile);
    }

    public MapDbAppFileSystemConfig(ConfigVersion version, String driveName, boolean remotelyAccessible, Path dbFile) {
        this(driveName, remotelyAccessible, dbFile);
        this.version = version;
    }

    public Path getDbFile() {
        return dbFile;
    }

    public MapDbAppFileSystemConfig setDbFile(Path dbFile) {
        this.dbFile = checkDbFile(dbFile);
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
