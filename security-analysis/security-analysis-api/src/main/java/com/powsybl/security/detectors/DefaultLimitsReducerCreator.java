/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultLimitsReducerCreator extends AbstractLimitsReducerCreator<LoadingLimits, DefaultLimitsReducer> {
    private final Network dummyNetwork;

    public DefaultLimitsReducerCreator() {
        //TODO Find a better way to create LoadingLimits not linked to an element of the real network with the API.
        dummyNetwork = Network.create("Tmp", "Manual");
        VoltageLevel voltageLevel = dummyNetwork.newVoltageLevel().setId("vl").setNominalV(225.).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        voltageLevel.getBusBreakerView().newBus().setId("bus").add();
    }

    @Override
    protected DefaultLimitsReducer create(String networkElementId, LoadingLimits originalLimits) {
        //TODO Find a better way with the API to create LoadingLimits not linked to an element of the real network.
        DanglingLine danglingLine = dummyNetwork.getDanglingLine(networkElementId);
        if (danglingLine == null) {
            danglingLine = dummyNetwork.getVoltageLevel("vl").newDanglingLine().setId(networkElementId).setConnectableBus("bus")
                    .setR(0.).setX(0).setG(0.).setB(0.).setP0(0.).setQ0(0.).add();
        }
        return new DefaultLimitsReducer(originalLimits, danglingLine.newOperationalLimitsGroup("Reduced limits"));
    }
}
