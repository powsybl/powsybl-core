/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum IdentifiableType {
    NETWORK(null),
    SUBSTATION(null),
    VOLTAGE_LEVEL(null),
    HVDC_LINE(null),
    BUS(null),
    SWITCH(null),
    BUSBAR_SECTION(ConnectableType.BUSBAR_SECTION),
    LINE(ConnectableType.LINE),
    TWO_WINDINGS_TRANSFORMER(ConnectableType.TWO_WINDINGS_TRANSFORMER),
    THREE_WINDINGS_TRANSFORMER(ConnectableType.THREE_WINDINGS_TRANSFORMER),
    GENERATOR(ConnectableType.GENERATOR),
    BATTERY(ConnectableType.BATTERY),
    LOAD(ConnectableType.LOAD),
    SHUNT_COMPENSATOR(ConnectableType.SHUNT_COMPENSATOR),
    DANGLING_LINE(ConnectableType.DANGLING_LINE),
    STATIC_VAR_COMPENSATOR(ConnectableType.STATIC_VAR_COMPENSATOR),
    HVDC_CONVERTER_STATION(ConnectableType.HVDC_CONVERTER_STATION);

    private final ConnectableType connectableType;

    IdentifiableType(ConnectableType connectableType) {
        this.connectableType = connectableType;
    }

    public Optional<ConnectableType> getConnectableType() {
        return Optional.ofNullable(connectableType);
    }
}
