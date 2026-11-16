/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming;

import java.util.UUID;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface NamingStrategyProvider {
    /**
     * The public name used to select this naming strategy (e.g., "identity", "cgmes").
     */
    String getName();

    /**
     * Create a new NamingStrategy instance. Implementations may use the provided UUID namespace
     * to generate stable identifiers.
     */
    NamingStrategy create(UUID uuidNamespace);
}
