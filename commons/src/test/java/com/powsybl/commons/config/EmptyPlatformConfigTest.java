/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class EmptyPlatformConfigTest {

    @Test
    void test() {
        assertEquals(Optional.empty(), PlatformConfig.defaultConfig().getConfigDir());
        assertEquals(Optional.empty(), PlatformConfig.defaultConfig().getOptionalModuleConfig("any"));
        assertEquals("com.powsybl.commons.config.PlatformConfig$EmptyModuleConfigRepository", PlatformConfig.defaultConfig().getRepository().getClass().getName());
    }

}
