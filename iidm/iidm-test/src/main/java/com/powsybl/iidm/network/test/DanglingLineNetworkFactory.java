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
            .setNominalV(100.0f)
            .setLowVoltageLimit(80.0f)
            .setHighVoltageLimit(120.0f)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        voltageLevel.getBusBreakerView().newBus()
            .setId("BUS")
            .add();
        voltageLevel.newGenerator()
            .setId("G")
            .setMinP(0)
            .setMaxP(100)
            .setVoltageRegulatorOn(true)
            .setTargetV(100.0f)
            .setTargetP(50.0f)
            .setTargetQ(30.0f)
            .setBus("BUS")
            .add();
        DanglingLine danglingLine = voltageLevel.newDanglingLine()
            .setId("DL")
            .setBus("BUS")
            .setR(10.0f)
            .setX(1.0f)
            .setB(10e-6f)
            .setG(10e-5f)
            .setP0(50.0f)
            .setQ0(30.0f)
            .add();
        danglingLine.newCurrentLimits()
            .setPermanentLimit(100.0f)
            .beginTemporaryLimit()
                .setName("20'")
                .setValue(120.0f)
                .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
                .setName("10'")
                .setValue(140.0f)
                .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        return network;
    }
}
