/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class DanglingLineNetworkFactory {

    private DanglingLineNetworkFactory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("dangling-line", "test");

        Substation substation = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
            .setId("VL")
            .setNominalV(100.0)
            .setLowVoltageLimit(80.0)
            .setHighVoltageLimit(120.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        voltageLevel.getBusBreakerView().newBus()
            .setId("BUS")
            .add();
        voltageLevel.newGenerator()
            .setId("G")
            .setMinP(0.0)
            .setMaxP(100.0)
            .setVoltageRegulatorOn(true)
            .setTargetV(100.0)
            .setTargetP(50.0)
            .setTargetQ(30.0)
            .setBus("BUS")
            .add();
        DanglingLine danglingLine = voltageLevel.newDanglingLine()
            .setId("DL")
            .setBus("BUS")
            .setR(10.0)
            .setX(1.0)
            .setB(10e-6)
            .setG(10e-5)
            .setP0(50.0)
            .setQ0(30.0)
            .add();
        danglingLine.newCurrentLimits()
            .setPermanentLimit(100.0)
            .beginTemporaryLimit()
                .setName("20'")
                .setValue(120.0)
                .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
                .setName("10'")
                .setValue(140.0)
                .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        return network;
    }
}
