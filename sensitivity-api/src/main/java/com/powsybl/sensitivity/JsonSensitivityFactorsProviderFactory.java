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
