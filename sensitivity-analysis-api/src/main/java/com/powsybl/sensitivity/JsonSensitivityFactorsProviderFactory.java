/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.io.InputStream;
import java.nio.file.Path;

public class JsonSensitivityFactorsProviderFactory implements SensitivityFactorsProviderFactory {
    @Override
    public JsonSensitivityFactorsProvider create() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("json-sensitivity-factors");
        Path jsonFile = config.getPathProperty("json-file");
        return new JsonSensitivityFactorsProvider(jsonFile);
    }

    @Override
    public JsonSensitivityFactorsProvider create(Path sensitivityFactorsFile) {
        return new JsonSensitivityFactorsProvider(sensitivityFactorsFile);
    }

    @Override
    public JsonSensitivityFactorsProvider create(InputStream data) {
        return new JsonSensitivityFactorsProvider(data);
    }
}
