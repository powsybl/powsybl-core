/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 *
 * @deprecated Use {@link IdentifiableType} instead.
 */
@Deprecated(since = "4.5.0")
public enum ConnectableType {
    BUSBAR_SECTION,
    LINE,
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
