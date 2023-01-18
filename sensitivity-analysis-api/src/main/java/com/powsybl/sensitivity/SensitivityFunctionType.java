/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import java.util.OptionalInt;

/**
 * Supported function types related to the equipment to monitor. Use:
 * - BRANCH_ACTIVE_POWER_1 and BRANCH_ACTIVE_POWER_2 if you want to monitor the active power in MW of a network branch (lines,
 * two windings transformer, dangling lines, etc.). Use 1 for side 1 and 2 for side 2. In case of a three windings transformer,
 * use BRANCH_ACTIVE_POWER_3 to monitor the active power in MW of the leg 3 (network side).
 * - BRANCH_CURRENT_1 and BRANCH_CURRENT_2 if you want to monitor the current in A of a network branch (lines,
 * two windings transformer, dangling lines, etc.). Use 1 for side 1 and use 2 for side 2. In case of a three windings transformer,
 * use BRANCH_CURRENT_3 to monitor the current in A of the leg 3 (network side).
 * - BUS_VOLTAGE if you want to monitor the voltage in KV of a specific network bus.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum SensitivityFunctionType {
    /**
     * @deprecated Use {@link #BRANCH_ACTIVE_POWER_1} instead.
     */
    @Deprecated BRANCH_ACTIVE_POWER, // MW
    /**
     * @deprecated Use {@link #BRANCH_CURRENT_1} instead.
     */
    @Deprecated BRANCH_CURRENT, // A
    BRANCH_ACTIVE_POWER_1(1), // MW
    BRANCH_CURRENT_1(1), // A
    BRANCH_ACTIVE_POWER_2(2), // MW
    BRANCH_CURRENT_2(2), // A
    BRANCH_ACTIVE_POWER_3(3), // MW
    BRANCH_CURRENT_3(3), // A
    BUS_VOLTAGE; // KV

    private final Integer side;

    SensitivityFunctionType() {
        this.side = null;
    }

    SensitivityFunctionType(int side) {
        this.side = side;
    }

    public OptionalInt getSide() {
        return side == null ? OptionalInt.empty() : OptionalInt.of(side);
    }
}
