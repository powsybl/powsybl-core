/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

/**
 * A variable represents a change on a equipment or on a group of equipments. The supported variable types are:
 * - Use INJECTION_ACTIVE_POWER to model a change on the production of a generator or on a group of generators, on
 * the consumption of a load or on a group of loads or on GLSK (for Generation and Load Shift keys) that describes a
 * linear combination of power injection shifts on generators and loads. The variable increase is in MW.
 * - Use TRANSFORMER_PHASE to model the change of the tap position of a phase tap changer. The increase is in degree.
 * - Use BUS_TARGET_VOLTAGE to model an increase of the voltage target of a generator, a static var compensator, a two
 * or three windings transformer, a shunt compensator or a VSC converter station. The increase is in KV.
 * - Use HVDC_LINE_ACTIVE_POWER to model the change of the active power set point of an HVDC line. The increase is in MW.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum SensitivityVariableType {
    INJECTION_ACTIVE_POWER, // MW
    TRANSFORMER_PHASE, // °
    BUS_TARGET_VOLTAGE, // KV
    HVDC_LINE_ACTIVE_POWER // MW
}
