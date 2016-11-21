/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.ConnectableType;
import eu.itesla_project.iidm.network.HvdcConverterStation;
import eu.itesla_project.iidm.network.Terminal;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
abstract class HvdcConverterStationImpl<T extends HvdcConverterStation<T>> extends ConnectableImpl<T> implements HvdcConverterStation<T> {

    HvdcConverterStationImpl(String id, String name) {
        super(id, name);
    }

    @Override
    public Terminal getTerminal() {
        return terminals.get(0);
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.HVDC_CONVERTER_STATION;
    }

}
