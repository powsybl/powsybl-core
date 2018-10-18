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

}
