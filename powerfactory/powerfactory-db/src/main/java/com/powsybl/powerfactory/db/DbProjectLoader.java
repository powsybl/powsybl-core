/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import com.google.auto.service.AutoService;
import com.google.common.base.Stopwatch;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.PowerFactoryDataLoader;
import com.powsybl.powerfactory.model.PowerFactoryException;
import com.powsybl.powerfactory.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(PowerFactoryDataLoader.class)
public class DbProjectLoader implements PowerFactoryDataLoader<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbProjectLoader.class);

    private final PlatformConfig platformConfig;

    private final DatabaseReader dbReader;

    public DbProjectLoader() {
        this(PlatformConfig.defaultConfig(), new JniDatabaseReader());
    }

    public DbProjectLoader(PlatformConfig platformConfig, DatabaseReader dbReader) {
        this.platformConfig = Objects.requireNonNull(platformConfig);
        this.dbReader = Objects.requireNonNull(dbReader);
    }

    @Override
    public Class<Project> getDataClass() {
        return Project.class;
    }

    @Override
    public String getExtension() {
        return "properties";
    }

    @Override
    public boolean test(InputStream is) {
        return dbReader.isOk() && ActiveProject.read(is).isPresent();
    }

    @Override
    public Project doLoad(String fileName, InputStream is) {
        ActiveProject activeProject = ActiveProject.read(is)
                .orElseThrow(() -> new PowerFactoryException("Project name not found in property file '" + fileName + "'"));
        LOGGER.info("Working on project '{}'", activeProject.getName());

        Path powerFactoryHomeDir = PowerFactoryAppUtil.getHomeDir(platformConfig);
        LOGGER.info("Using PowerFactory installation '{}'", powerFactoryHomeDir);

        // read study cases objects from PowerFactory DB using C++ API
        DataObjectBuilder builder = new DataObjectBuilder();
        LOGGER.info("Loading objects from DB...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        dbReader.read(powerFactoryHomeDir.toString(), activeProject.getName(), builder);
        LOGGER.info("{} objects loaded in {} s", builder.getIndex().getDataObjects().size(), stopwatch.elapsed(TimeUnit.SECONDS));

        DataObject rootObject = builder.getIndex().getDataObjectById(0L)
                .orElseThrow(() -> new PowerFactoryException("Root object not found"));
        Instant creationTime = Instant.now(); // FIXME get from root object
        return new Project(activeProject.getName(), creationTime, rootObject, builder.getIndex());
    }
}
