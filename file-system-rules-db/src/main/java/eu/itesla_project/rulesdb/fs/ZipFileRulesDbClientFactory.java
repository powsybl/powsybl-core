/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.rulesdb.fs;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.modules.rules.RulesDbClient;
import eu.itesla_project.modules.rules.RulesDbClientFactory;
import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ZipFileRulesDbClientFactory implements RulesDbClientFactory {

    @Override
    public RulesDbClient create(String rulesDbName) {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("zipfilerulesdb");
        Path zipDir = config.getPathProperty("zipDir");
        Path zipFile = zipDir.resolve(rulesDbName + ".zip");
        return new ZipFileRulesDbClient(zipFile);
    }

}
