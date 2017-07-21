/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.mapdb;

import eu.itesla_project.afs.core.AfsException;
import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbAppFileSystemConfig {

    private String driveName;

    private Path dbFile;

    public static List<MapDbAppFileSystemConfig> load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static List<MapDbAppFileSystemConfig> load(PlatformConfig platformConfig) {
        List<MapDbAppFileSystemConfig> configs = new ArrayList<>();
        ModuleConfig moduleConfig = platformConfig.getModuleConfigIfExists("mapdb-app-file-system");
        if (moduleConfig != null) {
            if (moduleConfig.hasProperty("drive-name") && moduleConfig.hasProperty("db-file")) {
                String driveName = moduleConfig.getStringProperty("drive-name");
                Path rootDir = moduleConfig.getPathProperty("db-file");
                configs.add(new MapDbAppFileSystemConfig(driveName, rootDir));
            }
            int maxAdditionalDriveCount = moduleConfig.getIntProperty("max-additional-drive-count", 0);
            for (int i = 0; i < maxAdditionalDriveCount; i++) {
                if (moduleConfig.hasProperty("drive-name-" + i) && moduleConfig.hasProperty("db-file-" + i)) {
                    String driveName = moduleConfig.getStringProperty("drive-name-" + i);
                    Path rootDir = moduleConfig.getPathProperty("db-file-" + i);
                    configs.add(new MapDbAppFileSystemConfig(driveName, rootDir));
                }
            }
        }
        return configs;
    }

    private static Path checkDbFile(Path dbFile) {
        Objects.requireNonNull(dbFile);
        if (Files.isDirectory(dbFile)) {
            throw new AfsException("DB file " + dbFile + " is a directory");
        }
        return dbFile;
    }

    public MapDbAppFileSystemConfig(String driveName, Path dbFile) {
        this.driveName = Objects.requireNonNull(driveName);
        this.dbFile = checkDbFile(dbFile);
    }

    public String getDriveName() {
        return driveName;
    }

    public MapDbAppFileSystemConfig setDriveName(String driveName) {
        this.driveName = Objects.requireNonNull(driveName);
        return this;
    }

    public Path getDbFile() {
        return dbFile;
    }

    public MapDbAppFileSystemConfig setDbFile(Path dbFile) {
        this.dbFile = checkDbFile(dbFile);
        return this;
    }
}
