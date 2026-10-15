/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import java.util.Optional;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ModuleConfigRepository {

    Optional<ModuleConfig> getModuleConfig(String name);

    /**
     * Names of the modules this repository defines, when it can enumerate them. The default
     * implementation returns an empty set, meaning "unknown or not enumerable": it is used for
     * diagnostics only (see {@link RuntimeConfigRepositories}) and never to resolve a value.
     */
    default Set<String> getModuleNames() {
        return Set.of();
    }
}
