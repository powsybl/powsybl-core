/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.powerfactory.model.PowerFactoryException;
import com.powsybl.powerfactory.model.StudyCase;
import com.powsybl.powerfactory.model.PowerFactoryDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.Properties;

/**
 * Study case loader that is able to connect to PowerFactory DB using C++ API.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(PowerFactoryDataLoader.class)
public class DbStudyCaseLoader implements PowerFactoryDataLoader<StudyCase> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbStudyCaseLoader.class);

    private final PlatformConfig platformConfig;

    private final DatabaseReader dbReader;

    public DbStudyCaseLoader() {
        this(PlatformConfig.defaultConfig(), new JniDatabaseReader());
    }

    public DbStudyCaseLoader(PlatformConfig platformConfig, DatabaseReader dbReader) {
        this.platformConfig = Objects.requireNonNull(platformConfig);
        this.dbReader = Objects.requireNonNull(dbReader);
    }

    @Override
    public Class<StudyCase> getDataClass() {
        return StudyCase.class;
    }

    @Override
    public String getExtension() {
        return "properties";
    }

    private static String readProjectName(InputStream is) {
        try (is) {
            var properties = new Properties();
            properties.load(is);
            return properties.getProperty("projectName");
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean test(InputStream is) {
        return dbReader.isOk() && readProjectName(is) != null;
    }

    @Override
    public StudyCase doLoad(String fileName, InputStream is) {
        String projectName = readProjectName(is);
        if (projectName == null) {
            throw new PowerFactoryException("Project name not found in property file '" + fileName + "'");
        }
        LOGGER.info("Working on project '{}'", projectName);

        Path powerFactoryHomeDir = PowerFactoryAppUtil.getHomeDir(platformConfig);
        LOGGER.info("Using PowerFactory installation '{}'", powerFactoryHomeDir);

        // read study cases objects from PowerFactory DB using C++ API
        DataObjectBuilder builder = new DataObjectBuilder();
        dbReader.read(powerFactoryHomeDir.toString(), projectName, builder);

        Instant time = Instant.now(); // FIXME get from study case object
        String studyCaseName = "???";
        return new StudyCase(studyCaseName, time, builder.getIndex());
    }
}
