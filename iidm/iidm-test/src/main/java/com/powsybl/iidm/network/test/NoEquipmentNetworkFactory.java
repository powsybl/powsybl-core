/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;

public final class NoEquipmentNetworkFactory {

    private NoEquipmentNetworkFactory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("test", "test");
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
}
