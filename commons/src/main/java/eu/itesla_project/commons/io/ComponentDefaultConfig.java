/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ComponentDefaultConfig {

    private final ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("componentDefaultConfig");

    public <T, U extends T> Class<? extends T> findFactoryImplClass(Class<T> factoryBaseClass) {
        return config.getClassProperty(factoryBaseClass.getName(), factoryBaseClass);
    }
}
