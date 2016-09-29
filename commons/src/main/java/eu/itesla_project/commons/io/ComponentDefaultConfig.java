/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ComponentDefaultConfig {

    private final ModuleConfig config;

    public ComponentDefaultConfig() {
        this(PlatformConfig.defaultConfig().getModuleConfig("componentDefaultConfig"));
    }

    public ComponentDefaultConfig(ModuleConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    public <T, U extends T> Class<? extends T> findFactoryImplClass(Class<T> factoryBaseClass) {
        String propertyName = factoryBaseClass.getSimpleName();
        return config.getClassProperty(propertyName, factoryBaseClass);
    }
}
