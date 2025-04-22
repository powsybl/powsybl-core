/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum IdentifiableType {
    NETWORK,
    SUBSTATION,
    VOLTAGE_LEVEL,
    AREA,
    HVDC_LINE,
    BUS,
    SWITCH,
    BUSBAR_SECTION,
    LINE,
    TIE_LINE,
    TWO_WINDINGS_TRANSFORMER,
    THREE_WINDINGS_TRANSFORMER,
    GENERATOR,
    BATTERY,
    LOAD,
    SHUNT_COMPENSATOR,
    DANGLING_LINE,
    STATIC_VAR_COMPENSATOR,
    HVDC_CONVERTER_STATION,
    OVERLOAD_MANAGEMENT_SYSTEM,
    GROUND,
    DC_NODE,
    DC_SWITCH,
    DC_GROUND,
    DC_LINE,
    DC_LINE_COMMUTATED_CONVERTER,
    DC_VOLTAGE_SOURCE_CONVERTER
}
