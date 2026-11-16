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

import java.io.InputStream;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(PowerFactoryDataLoader.class)
public class DbProjectLoader implements PowerFactoryDataLoader<Project> {

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
        return dbReader.isOk() && ActiveProjectConfig.read(is).isPresent();
    }

    @Override
    public Project doLoad(String fileName, InputStream is) {
        ActiveProjectConfig activeProjectConfig = ActiveProjectConfig.read(is)
                .orElseThrow(() -> new PowerFactoryException("Project name not found in property file '" + fileName + "'"));

        return activeProjectConfig.loadProjectFromDb(dbReader, platformConfig);
    }
}
