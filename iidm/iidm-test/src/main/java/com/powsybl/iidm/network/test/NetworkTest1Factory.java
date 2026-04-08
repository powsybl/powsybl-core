/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class NetworkTest1Factory {

    private NetworkTest1Factory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault(), null);
    }

    public static Network create(String networkId) {
        return create(NetworkFactory.findDefault(), networkId);
    }

    public static Network create(NetworkFactory networkFactory, String nid) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork(id("network", nid), "test");
        Substation substation1 = network.newSubstation()
                .setId(id("substation1", nid))
                .setCountry(Country.FR)
                .setTso(id("TSO1", nid))
                .setGeographicalTags(id("region1", nid))
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId(id("voltageLevel1", nid))
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel.NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();
        BusbarSection voltageLevel1BusbarSection1 = topology1.newBusbarSection()
                .setId(id("voltageLevel1BusbarSection1", nid))
                .setNode(0)
                .add();
        BusbarSection voltageLevel1BusbarSection2 = topology1.newBusbarSection()
                .setId(id("voltageLevel1BusbarSection2", nid))
                .setNode(1)
                .add();
        topology1.newBreaker()
                .setId(id("voltageLevel1Breaker1", nid))
                .setRetained(true)
                .setOpen(false)
                .setNode1(voltageLevel1BusbarSection1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1BusbarSection2.getTerminal().getNodeBreakerView().getNode())
                .add();
        Load load1 = voltageLevel1.newLoad()
                .setId(id("load1", nid))
                .setNode(2)
                .setP0(10)
                .setQ0(3)
                .add();
        topology1.newDisconnector()
                .setId(id("load1Disconnector1", nid))
                .setOpen(false)
                .setNode1(load1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(3)
                .add();
        topology1.newDisconnector()
                .setId(id("load1Breaker1", nid))
                .setOpen(false)
                .setNode1(3)
                .setNode2(voltageLevel1BusbarSection1.getTerminal().getNodeBreakerView().getNode())
                .add();
        Generator generator1 = voltageLevel1.newGenerator()
                .setId(id("generator1", nid))
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
                .setId(id("generator1Disconnector1", nid))
                .setOpen(false)
                .setNode1(generator1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(6)
                .add();
        topology1.newDisconnector()
                .setId(id("generator1Breaker1", nid))
                .setOpen(false)
                .setNode1(6)
                .setNode2(voltageLevel1BusbarSection2.getTerminal().getNodeBreakerView().getNode())
                .add();
        return network;
    }

    public static String id(String localId, String networkId) {
        return networkId != null ? "n" + networkId + "_" + localId : localId;
    }
}
