/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 * Simple {@link NetworkModification} for a static var compensator.
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public class StaticVarCompensatorModification extends AbstractSetpointModification<StaticVarCompensator> {

    public StaticVarCompensatorModification(String elementId, Double voltageSetpoint, Double reactivePowerSetpoint) {
        super(elementId, voltageSetpoint, reactivePowerSetpoint);
    }

    @Override
    public String getName() {
        return "StaticVarCompensatorModification";
    }

    @Override
    public String getElementName() {
        return "StaticVarCompensator";
    }

    @Override
    protected void setVoltageSetpoint(StaticVarCompensator networkElement, Double voltageSetpoint) {
        networkElement.setVoltageSetpoint(voltageSetpoint);
    }

    @Override
    protected void setReactivePowerSetpoint(StaticVarCompensator networkElement, Double reactivePowerSetpoint) {
        networkElement.setReactivePowerSetpoint(reactivePowerSetpoint);
    }

    @Override
    public StaticVarCompensator getNetworkElement(Network network, String elementID) {
        return network.getStaticVarCompensator(elementID);
    }

    public String getStaticVarCompensatorId() {
        return getElementId();
    }
}
