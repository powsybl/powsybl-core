/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.commons.ref.Ref;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class VoltageAngleLimitAdderImpl implements VoltageAngleLimitAdder, Validable {

    private final NetworkImpl network;
    private final String subnetwork;
    private String id;
    private Terminal from;
    private Terminal to;
    private double lowLimit = Double.NaN;

    private double highLimit = Double.NaN;

    VoltageAngleLimitAdderImpl(NetworkImpl network, String subnetwork) {
        this.network = network;
        this.subnetwork = subnetwork;
    }

    @Override
    public VoltageAngleLimitAdderImpl setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl from(Terminal from) {
        this.from = from;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl to(Terminal to) {
        this.to = to;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl setLowLimit(double lowLimit) {
        this.lowLimit = lowLimit;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl setHighLimit(double highLimit) {
        this.highLimit = highLimit;
        return this;
    }

    @Override
    public VoltageAngleLimit add() {
        if (id == null) {
            throw new IllegalStateException("Voltage angle limit id is mandatory.");
        }
        if (subnetwork != null && checkTerminalsInSubnetwork(subnetwork)) {
            throw new ValidationException(this, "The involved voltage levels are not in the subnetwork '" +
                    subnetwork + "'. Create this VoltageAngleLimit from the parent network '" + network.getId() + "'");
        }
        if (network.getVoltageAngleLimitsIndex().containsKey(id)) {
            throw new PowsyblException("The network " + network.getId()
                    + " already contains a voltage angle limit with the id '" + id + "'");
        }
        if (!Double.isNaN(lowLimit) && !Double.isNaN(highLimit) && lowLimit >= highLimit) {
            throw new IllegalStateException("Voltage angle low limit must be lower than the high limit.");
        }

        VoltageLevelExt voltageLevel1 = (VoltageLevelExt) from.getVoltageLevel();
        VoltageLevelExt voltageLevel2 = (VoltageLevelExt) to.getVoltageLevel();
        Ref<NetworkImpl> networkRef = AbstractIdentifiableAdder.computeNetworkRef(network, voltageLevel1, voltageLevel2);

        VoltageAngleLimit voltageAngleLimit = new VoltageAngleLimitImpl(id, from, to, lowLimit, highLimit, networkRef);
        networkRef.get().getVoltageAngleLimitsIndex().put(id, voltageAngleLimit);
        return voltageAngleLimit;
    }

    private boolean checkTerminalsInSubnetwork(String subnetworkId) {
        return !subnetworkId.equals(from.getVoltageLevel().getParentNetwork().getId())
                || !subnetworkId.equals(to.getVoltageLevel().getParentNetwork().getId());
    }

    @Override
    public String getMessageHeader() {
        return "VoltageAngleLimit '" + id + "': ";
    }
}
