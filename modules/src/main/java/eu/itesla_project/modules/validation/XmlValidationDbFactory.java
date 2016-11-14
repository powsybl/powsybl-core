/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.validation;

import eu.itesla_project.commons.config.PlatformConfig;

import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlValidationDbFactory implements ValidationDbFactory {

    @Override
    public ValidationDb create() {
        Path dir = PlatformConfig.defaultConfig().getModuleConfig("xml-validation-db").getPathProperty("directory");
        return new XmlValidationDb(dir);
    }

}
