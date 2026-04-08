/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming.mock;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.conversion.naming.NamingStrategyProvider;

import java.util.UUID;

/**
 * Provider for the mock strategy (test scope only).
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(NamingStrategyProvider.class)
public class MockNamingStrategyProvider implements NamingStrategyProvider {

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public NamingStrategy create(UUID uuidNamespace) {
        return new MockNamingStrategy();
    }
}
