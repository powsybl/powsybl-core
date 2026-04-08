/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.db;

import com.google.common.base.Stopwatch;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.PowerFactoryException;
import com.powsybl.powerfactory.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ActiveProjectConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveProjectConfig.class);

    private final String name;

    public ActiveProjectConfig(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public static Optional<ActiveProjectConfig> read(InputStream is) {
        try (is) {
            var properties = new Properties();
            properties.load(is);
            return Optional.ofNullable(properties.getProperty("projectName"))
                    .map(ActiveProjectConfig::new);
        } catch (IOException e) {
            return Optional.empty();
        }

    }

    public Project loadProjectFromDb() {
        return loadProjectFromDb(new JniDatabaseReader(), PlatformConfig.defaultConfig());
    }

    public Project loadProjectFromDb(DatabaseReader dbReader, PlatformConfig platformConfig) {
        LOGGER.info("Working on project '{}'", name);

        Path powerFactoryHomeDir = PowerFactoryAppUtil.getHomeDir(platformConfig);
        LOGGER.info("Using PowerFactory installation '{}'", powerFactoryHomeDir);

        // read study cases objects from PowerFactory DB using C++ API
        DataObjectBuilder builder = new DataObjectBuilder();
        LOGGER.info("Loading objects from DB...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        dbReader.read(powerFactoryHomeDir.toString(), name, builder);
        LOGGER.info("{} objects loaded in {} s", builder.getIndex().getDataObjects().size(), stopwatch.elapsed(TimeUnit.SECONDS));

        DataObject rootObject = builder.getIndex().getDataObjectById(0L)
                .orElseThrow(() -> new PowerFactoryException("Root object not found"));
        return new Project(name, rootObject, builder.getIndex());
    }
}
