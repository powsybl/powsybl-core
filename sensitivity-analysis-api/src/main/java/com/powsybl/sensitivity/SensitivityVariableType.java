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
 * A variable represents a change on a equipment or on a group of equipments. The supported variable types are:
 * <ul>
 *     <li>Use {@link #INJECTION_ACTIVE_POWER} to model a change on the production of a generator or on a group of generators, on
 * the consumption of a load or on a group of loads or on GLSK (for Generation and Load Shift keys) that describes a
 * linear combination of power injection shifts on generators and loads. The variable increase is in MW.</li>
 *     <li>Use {@link #INJECTION_REACTIVE_POWER} to model a change on the reactive production of a generator or on
 * the reactive consumption of a load. The variable increase is in MVar.</li>
 *     <li>Use {@link #TRANSFORMER_PHASE} to model the change of the tap position of a phase tap changer of a two windings transformer
 * or a three windings transformer that contains only one phase tap changer. The increase is in degree.</li>
 *     <li>Use {@link #BUS_TARGET_VOLTAGE} to model an increase of the voltage target of a generator, a static var compensator, a two
 * or three windings transformer, a shunt compensator or a VSC converter station. The increase is in KV.</li>
 *     <li>Use {@link #HVDC_LINE_ACTIVE_POWER} to model the change of the active power set point of an HVDC line. The increase is in MW.</li>
 *     <li>Use {@link #TRANSFORMER_PHASE_1}, {@link #TRANSFORMER_PHASE_2} or {@link #TRANSFORMER_PHASE_3} to model the change of the tap position of a phase
 * tap changer of a three windings transformer that contains several phase tap changers.</li>
 * </ul>
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum SensitivityVariableType {
    /** increase in MW */
    INJECTION_ACTIVE_POWER,
    /** increase in MVar */
    INJECTION_REACTIVE_POWER,
    /** increase in degrees */
    TRANSFORMER_PHASE,
    /** increase in kV */
    BUS_TARGET_VOLTAGE,
    /** increase in MW */
    HVDC_LINE_ACTIVE_POWER,
    /** increase in degrees */
    TRANSFORMER_PHASE_1(1),
    /** increase in degrees */
    TRANSFORMER_PHASE_2(2),
    /** increase in degrees */
    TRANSFORMER_PHASE_3(3);

    private final Integer side;

    SensitivityVariableType() {
        this.side = null;
    }

    SensitivityVariableType(int side) {
        this.side = side;
    }

    public OptionalInt getSide() {
        return side == null ? OptionalInt.empty() : OptionalInt.of(side);
    }
}
