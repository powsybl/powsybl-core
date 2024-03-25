/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import java.util.OptionalInt;

/**
 * Supported function types related to the equipment to monitor. Use:
 * <ul>
 *     <li>{@link #BRANCH_ACTIVE_POWER_1} and {@link #BRANCH_ACTIVE_POWER_2} if you want to monitor the active power in MW of a network branch (lines,
 * two windings transformer, dangling lines, etc.). Use 1 for side 1 and 2 for side 2. In case of a three windings transformer,
 * use {@link #BRANCH_ACTIVE_POWER_3} to monitor the active power in MW of the leg 3 (network side).</li>
 *     <li>{@link #BRANCH_CURRENT_1} and {@link #BRANCH_CURRENT_2} if you want to monitor the current in A of a network branch (lines,
 * two windings transformer, dangling lines, etc.). Use 1 for side 1 and use 2 for side 2. In case of a three windings transformer,
 * use {@link #BRANCH_CURRENT_3} to monitor the current in A of the leg 3 (network side).</li>
 *     <li>{@link #BRANCH_REACTIVE_POWER_1} and {@link #BRANCH_REACTIVE_POWER_2} if you want to monitor the reactive power in MVar of a network branch (lines,
 * two windings transformer, dangling lines, etc.). Use 1 for side 1 and use 2 for side 2. In case of a three windings transformer,
 * use {@link #BRANCH_REACTIVE_POWER_3} to monitor the reactive power in MVar of the leg 3 (network side).</li>
 *     <li>{@link #BUS_REACTIVE_POWER} if you want to monitor the reactive power injection in MVar of a specific network bus</li>
 *     <li>{@link #BUS_VOLTAGE} if you want to monitor the voltage in KV of a specific network bus.</li>
 * </ul>
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum SensitivityFunctionType {

    /** in MW */
    BRANCH_ACTIVE_POWER_1(1),
    /** in A */
    BRANCH_CURRENT_1(1),
    /** in MVar */
    BRANCH_REACTIVE_POWER_1(1),
    /** in MW */
    BRANCH_ACTIVE_POWER_2(2),
    /** in A */
    BRANCH_CURRENT_2(2),
    /** in MVar */
    BRANCH_REACTIVE_POWER_2(2),
    /** in MW */
    BRANCH_ACTIVE_POWER_3(3),
    /** in A */
    BRANCH_CURRENT_3(3),
    /** in MVar */
    BRANCH_REACTIVE_POWER_3(3),
    /** In MVar */
    BUS_REACTIVE_POWER,
    /** in kV */
    BUS_VOLTAGE;

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
