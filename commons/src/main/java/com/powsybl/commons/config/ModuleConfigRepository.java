/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ModuleConfigRepository {
    /**
     * @deprecated Use the <code>Optional</code> returned by {@link #getModuleConfig(String)}
     */
    @Deprecated(since = "4.9.0")
    default boolean moduleExists(String name) {
        return getModuleConfig(name).isPresent();
    }

    Optional<ModuleConfig> getModuleConfig(String name);
}
