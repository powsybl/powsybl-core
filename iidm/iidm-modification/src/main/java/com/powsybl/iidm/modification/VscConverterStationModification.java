/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VscConverterStation;

/**
 * Simple {@link NetworkModification} for a VSC converter station.
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public class VscConverterStationModification extends AbstractSetpointModification<VscConverterStation> {

    public VscConverterStationModification(String elementId, Double voltageSetpoint, Double reactivePowerSetpoint) {
        super(elementId, voltageSetpoint, reactivePowerSetpoint);
    }

    @Override
    public String getName() {
        return "VscConverterStationModification";
    }

    @Override
    public String getElementName() {
        return "VscConverterStation";
    }

    @Override
    protected void setVoltageSetpoint(VscConverterStation networkElement, Double voltageSetpoint) {
        networkElement.setVoltageSetpoint(voltageSetpoint);
    }

    @Override
    protected void setReactivePowerSetpoint(VscConverterStation networkElement, Double reactivePowerSetpoint) {
        networkElement.setReactivePowerSetpoint(reactivePowerSetpoint);
    }

    @Override
    public VscConverterStation getNetworkElement(Network network, String elementID) {
        return network.getVscConverterStation(elementID);
    }

    public String getVscConverterStationId() {
        return getElementId();
    }
}
