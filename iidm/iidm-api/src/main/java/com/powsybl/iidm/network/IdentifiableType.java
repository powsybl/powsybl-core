/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum IdentifiableType {
    NETWORK,
    SUBSTATION,
    VOLTAGE_LEVEL,
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
    HVDC_CONVERTER_STATION
}
