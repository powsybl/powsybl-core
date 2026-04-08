/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.db;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.powerfactory.model.PowerFactoryDataLoader;
import com.powsybl.powerfactory.model.PowerFactoryException;
import com.powsybl.powerfactory.model.Project;
import com.powsybl.powerfactory.model.StudyCase;

import java.io.InputStream;
import java.util.Objects;

/**
 * Study case loader that is able to connect to PowerFactory DB using C++ API.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(PowerFactoryDataLoader.class)
public class DbStudyCaseLoader implements PowerFactoryDataLoader<StudyCase> {

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

    @Override
    public boolean test(InputStream is) {
        return dbReader.isOk() && ActiveProjectConfig.read(is).isPresent();
    }

    @Override
    public StudyCase doLoad(String fileName, InputStream is) {
        ActiveProjectConfig activeProjectConfig = ActiveProjectConfig.read(is)
                .orElseThrow(() -> new PowerFactoryException("Project name not found in property file '" + fileName + "'"));

        Project project = activeProjectConfig.loadProjectFromDb(dbReader, platformConfig);
        return project.getActiveStudyCase();
    }
}
