/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.*;

/**
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
public final class ScalableTestNetwork {
    private ScalableTestNetwork() {
        throw new AssertionError("No default constructor in utility class");
    }

    public static Network createNetwork() {
        Network network = Network.create("network", "test");
        Substation s = network.newSubstation()
                .setId("s")
                .setCountry(Country.US)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380.0)
                .setLowVoltageLimit(0.8 * 380.0)
                .setHighVoltageLimit(1.2 * 380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("bus1")
                .add();
        vl.newGenerator()
                .setId("g1")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(0.0)
                .add();
        vl.newGenerator()
                .setId("g2")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(0.0)
                .add();
        vl.newGenerator()
                .setId("g3")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(1.0)
                .add();
        vl.newLoad()
                .setId("l1")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setP0(100.0)
                .setQ0(0.0)
                .setLoadType(LoadType.UNDEFINED)
                .add();

        return network;
    }
}
