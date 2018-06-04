/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class NetworkTest1Factory {

    private NetworkTest1Factory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("network1", "test");
        Substation substation1 = network.newSubstation()
                .setId("substation1")
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region1")
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("voltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel.NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();
        topology1.setNodeCount(10);
        BusbarSection voltageLevel1BusbarSection1 = topology1.newBusbarSection()
                .setId("voltageLevel1BusbarSection1")
                .setNode(0)
                .add();
        BusbarSection voltageLevel1BusbarSection2 = topology1.newBusbarSection()
                .setId("voltageLevel1BusbarSection2")
                .setNode(1)
                .add();
        topology1.newBreaker()
                .setId("voltageLevel1Breaker1")
                .setRetained(true)
                .setOpen(false)
                .setNode1(voltageLevel1BusbarSection1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1BusbarSection2.getTerminal().getNodeBreakerView().getNode())
                .add();
        Load load1 = voltageLevel1.newLoad()
                .setId("load1")
                .setNode(2)
                .setP0(10)
                .setQ0(3)
                .add();
        topology1.newDisconnector()
                .setId("load1Disconnector1")
                .setOpen(false)
                .setNode1(load1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(3)
                .add();
        topology1.newDisconnector()
                .setId("load1Breaker1")
                .setOpen(false)
                .setNode1(3)
                .setNode2(voltageLevel1BusbarSection1.getTerminal().getNodeBreakerView().getNode())
                .add();
        Generator generator1 = voltageLevel1.newGenerator()
                .setId("generator1")
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(200.0)
                .setMaxP(900.0)
                .setVoltageRegulatorOn(true)
                .setTargetP(900.0)
                .setTargetV(380.0)
                .setNode(5)
                .add();
        generator1.newReactiveCapabilityCurve()
                .beginPoint().setP(200.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
                .beginPoint().setP(900.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
                .add();
        topology1.newDisconnector()
                .setId("generator1Disconnector1")
                .setOpen(false)
                .setNode1(generator1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(6)
                .add();
        topology1.newDisconnector()
                .setId("generator1Breaker1")
                .setOpen(false)
                .setNode1(6)
                .setNode2(voltageLevel1BusbarSection2.getTerminal().getNodeBreakerView().getNode())
                .add();
        return network;
    }

}
