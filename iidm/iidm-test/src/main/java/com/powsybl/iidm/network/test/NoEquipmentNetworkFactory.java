/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public final class NoEquipmentNetworkFactory {

    private NoEquipmentNetworkFactory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2016-10-18T10:06:00.000+02:00"));
        Substation substation = network.newSubstation()
                    .setId("sub")
                    .setCountry(Country.FR)
                    .setTso("RTE")
                .add();
        VoltageLevel voltageLevelA = substation.newVoltageLevel()
                    .setId("vl1")
                    .setName("vl1")
                    .setNominalV(440.0)
                    .setHighVoltageLimit(400.0)
                    .setLowVoltageLimit(200.0)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevelA.getBusBreakerView().newBus()
                    .setId("busA")
                    .setName("busA")
                .add();
        VoltageLevel voltageLevelB = substation.newVoltageLevel()
                    .setId("vl2").setName("vl2")
                    .setNominalV(200.0)
                    .setHighVoltageLimit(400.0)
                    .setLowVoltageLimit(200.0)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevelB.getBusBreakerView().newBus()
                    .setId("busB")
                    .setName("busB")
                .add();
        return network;
    }

    public static Network createWithDanglingLine() {
        Network network = create();
        network.getVoltageLevel("vl1").newDanglingLine()
                .setId("DL")
                .setUcteXnodeCode("ucte")
                .setConnectableBus("busA")
                .setB(1.0)
                .setG(2.0)
                .setP0(3.0)
                .setQ0(4.0)
                .setX(5.0)
                .setR(6.0)
                .add();
        return network;
    }

    public static Network createWithTieLine() {
        Network network = create();
        double r = 10.0;
        double r2 = 1.0;
        double x = 20.0;
        double x2 = 2.0;
        double hl1g1 = 30.0;
        double hl1g2 = 35.0;
        double hl1b1 = 40.0;
        double hl1b2 = 45.0;
        double hl2g1 = 130.0;
        double hl2g2 = 135.0;
        double hl2b1 = 140.0;
        double hl2b2 = 145.0;
        double xnodeP = 50.0;
        double xnodeQ = 60.0;

        // adder
        network.newTieLine().setId("testTie")
                .setName("testNameTie")
                .setVoltageLevel1("vl1")
                .setBus1("busA")
                .setConnectableBus1("busA")
                .setVoltageLevel2("vl2")
                .setBus2("busB")
                .setConnectableBus2("busB")
                .setUcteXnodeCode("ucte")
                .line1()
                    .setId("hl1")
                    .setName("half1_name")
                    .setR(r)
                    .setX(x)
                    .setB1(hl1b1)
                    .setB2(hl1b2)
                    .setG1(hl1g1)
                    .setG2(hl1g2)
                    .setXnodeQ(xnodeQ)
                    .setXnodeP(xnodeP)
                .line2()
                    .setId("hl2")
                    .setR(r2)
                    .setX(x2)
                    .setB1(hl2b1)
                    .setB2(hl2b2)
                    .setG1(hl2g1)
                    .setG2(hl2g2)
                    .setXnodeP(xnodeP)
                    .setXnodeQ(xnodeQ)
                .add();
        return network;
    }
}
