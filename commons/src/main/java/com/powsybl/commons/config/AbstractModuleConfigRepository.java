/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractModuleConfigRepository implements ModuleConfigRepository {

    protected final Map<String, MapModuleConfig> configs = new HashMap<>();

    public boolean moduleExists(String name) {
        return configs.containsKey(name);
    }

    public Optional<ModuleConfig> getModuleConfig(String name) {
        return Optional.ofNullable(configs.get(name));
    }
}
