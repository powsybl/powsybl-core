/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import java.nio.file.FileSystem;
import java.util.Map;
import java.util.Set;

import static com.powsybl.commons.config.EnvironmentModuleConfigRepository.SEPARATOR;
import static com.powsybl.commons.config.EnvironmentModuleConfigRepository.UPPER_UNDERSCORE_FORMATTER;

/**
 * A {@link ModuleConfig} designed to read property values
 * from the map of environment variables.
 *
 * For a configuration property named "property-name" in module "module-name",
 * the expected environment variables name is MODULE_NAME__PROPERTY_NAME.
 * CamelCase names are also translated to underscore-separated names.
 *
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public class EnvironmentMapModuleConfig extends MapModuleConfig {

    private final String prefix;

    public EnvironmentMapModuleConfig(Map<Object, Object> properties, FileSystem fs, String moduleName) {
        super(properties, fs);
        this.prefix = UPPER_UNDERSCORE_FORMATTER.apply(moduleName) + SEPARATOR;
    }

    private String toUpper(String name) {
        return prefix + UPPER_UNDERSCORE_FORMATTER.apply(name);
    }

    @Override
    public boolean hasProperty(String name) {
        return super.hasProperty(toUpper(name));
    }

    @Override
    protected Object getValue(String propertyName) {
        return super.getValue(toUpper(propertyName));
    }

    @Override
    public Set<String> getPropertyNames() {
        throw new UnsupportedOperationException();
    }
}
