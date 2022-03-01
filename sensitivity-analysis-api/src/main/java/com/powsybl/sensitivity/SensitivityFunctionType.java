/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

/**
 * Supported function types related to the equipment to monitor. Use:
 * - BRANCH_ACTIVE_POWER if you want to monitor the active power in MW of a network branch (lines,
 * two windings transformer, dangling lines, etc.).
 * - BRANCH_CURRENT if you want to monitor the current in A of a network branch (lines,
 * two windings transformer, dangling lines, etc.).
 * - BUS_VOLTAGE if you want to monitor the voltage in KV of a specific network bus.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum SensitivityFunctionType {
    BRANCH_ACTIVE_POWER, // MW
    BRANCH_CURRENT, // A
    BUS_VOLTAGE // KV
}
