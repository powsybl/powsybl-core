/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class ReactiveLimitsTestNetworkFactory {

    private ReactiveLimitsTestNetworkFactory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("ReactiveLimits", "???");
        network.setCaseDate(DateTime.parse("2016-01-01T10:00:00.000+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .setTso("RTE")
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B")
                .add();
        Generator g1 = vl.newGenerator()
                .setId("G1")
                .setEnergySource(EnergySource.OTHER)
                .setMaxP(10)
                .setMinP(0)
                .setVoltageRegulatorOn(true)
                .setTargetV(380)
                .setTargetP(10)
                .setBus("B")
                .setConnectableBus("B")
                .add();
        g1.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(5)
                .setMinQ(1)
                .setMaxQ(10)
                .endPoint()
                .beginPoint()
                .setP(10)
                .setMinQ(-10)
                .setMaxQ(1)
                .endPoint()
                .add();
        Generator g2 = vl.newGenerator()
                .setId("G2")
                .setEnergySource(EnergySource.OTHER)
                .setMaxP(10)
                .setMinP(0)
                .setVoltageRegulatorOn(true)
                .setTargetV(380)
                .setTargetP(10)
                .setBus("B")
                .setConnectableBus("B")
                .add();
        g2.newMinMaxReactiveLimits()
                .setMinQ(1)
                .setMaxQ(10)
                .add();

        return network;
    }

}
