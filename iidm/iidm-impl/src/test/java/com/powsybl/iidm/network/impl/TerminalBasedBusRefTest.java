/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class TerminalBasedBusRefTest {

    private static Set<String> getConnectedConnectableIds(Bus bus) {
        return bus.getConnectedTerminalStream()
            .map(Terminal::getConnectable)
            .map(Identifiable::getId)
            .collect(Collectors.toSet());
    }

    private static Network createNodeBreakerNetwork(int busbarNode, int generatorNode, int loadNode, int fictitiousLoadNode,
                                                    int loadBusbarNode,
                                                    boolean reverseConnectableOrder) {
        Network network = Network.create("network", "test");
        Substation substation = network.newSubstation().setId("S").setCountry(Country.FR).add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
            .setId("VL")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        if (reverseConnectableOrder) {
            addLoad(voltageLevel, "1-generated-load", fictitiousLoadNode, true);
            addLoad(voltageLevel, "a-load", loadNode, false);
            addGenerator(voltageLevel, generatorNode);
            addBusbarSection(voltageLevel, "0-busbar", busbarNode);
        } else {
            addBusbarSection(voltageLevel, "0-busbar", busbarNode);
            addGenerator(voltageLevel, generatorNode);
            addLoad(voltageLevel, "a-load", loadNode, false);
            addLoad(voltageLevel, "1-generated-load", fictitiousLoadNode, true);
        }
        addBusbarSection(voltageLevel, "00-isolated-busbar", 90);
        addLoad(voltageLevel, "02-isolated-generated", 91, true);
        addBusbarSection(voltageLevel, "00-load-busbar", loadBusbarNode);

        if (reverseConnectableOrder) {
            addClosedBreaker(voltageLevel, "SW-LOAD-GENERATED", loadNode, fictitiousLoadNode);
            addClosedBreaker(voltageLevel, "SW-GENERATOR-LOAD", generatorNode, loadNode);
            addClosedBreaker(voltageLevel, "SW-BUSBAR-GENERATOR", busbarNode, generatorNode);
            addClosedBreaker(voltageLevel, "SW-LOAD-BUSBAR", loadNode, loadBusbarNode);
        } else {
            addClosedBreaker(voltageLevel, "SW-BUSBAR-GENERATOR", busbarNode, generatorNode);
            addClosedBreaker(voltageLevel, "SW-GENERATOR-LOAD", generatorNode, loadNode);
            addClosedBreaker(voltageLevel, "SW-LOAD-GENERATED", loadNode, fictitiousLoadNode);
            addClosedBreaker(voltageLevel, "SW-LOAD-BUSBAR", loadNode, loadBusbarNode);
        }
        addClosedBreaker(voltageLevel, "SW-ISOLATED-BUSBAR", 90, 91);
        return network;
    }

    private static void addBusbarSection(VoltageLevel voltageLevel, String id, int node) {
        voltageLevel.getNodeBreakerView().newBusbarSection()
            .setId(id)
            .setNode(node)
            .add();
    }

    private static void addGenerator(VoltageLevel voltageLevel, int node) {
        voltageLevel.newGenerator()
            .setId("z-generator")
            .setNode(node)
            .setMinP(-100)
            .setMaxP(100)
            .setTargetP(0)
            .setLocalTargetV(400)
            .setLocalTargetQ(0)
            .add();
    }

    private static void addLoad(VoltageLevel voltageLevel, String id, int node, boolean fictitious) {
        voltageLevel.newLoad()
            .setId(id)
            .setNode(node)
            .setP0(1)
            .setQ0(1)
            .setFictitious(fictitious)
            .add();
    }

    private static void addClosedBreaker(VoltageLevel voltageLevel, String id, int node1, int node2) {
        voltageLevel.getNodeBreakerView().newBreaker()
            .setId(id)
            .setNode1(node1)
            .setNode2(node2)
            .setOpen(false)
            .add();
    }

    private static Network createBusBreakerNetwork(boolean reverseConnectableOrder) {
        Network network = Network.create("bus-breaker-network", "test");
        Substation substation = network.newSubstation().setId("S").setCountry(Country.FR).add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
            .setId("VL")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        voltageLevel.getBusBreakerView().newBus().setId("BUS").add();
        if (reverseConnectableOrder) {
            addBusBreakerLoad(voltageLevel, "00-generated-load", true);
            addBusBreakerLoad(voltageLevel, "a-load", false);
            addBusBreakerGenerator(voltageLevel);
        } else {
            addBusBreakerGenerator(voltageLevel);
            addBusBreakerLoad(voltageLevel, "a-load", false);
            addBusBreakerLoad(voltageLevel, "00-generated-load", true);
        }
        return network;
    }

    private static void addBusBreakerGenerator(VoltageLevel voltageLevel) {
        voltageLevel.newGenerator()
            .setId("z-generator")
            .setMinP(-100)
            .setMaxP(100)
            .setTargetP(0)
            .setLocalTargetV(400)
            .setLocalTargetQ(0)
            .setBus("BUS")
            .setConnectableBus("BUS")
            .add();
    }

    private static void addBusBreakerLoad(VoltageLevel voltageLevel, String id, boolean fictitious) {
        voltageLevel.newLoad()
            .setId(id)
            .setP0(1)
            .setQ0(1)
            .setFictitious(fictitious)
            .setBus("BUS")
            .setConnectableBus("BUS")
            .add();
    }

    private static Network createThreeWindingsTransformerNetwork() {
        Network network = Network.create("transformer-network", "test");
        Substation substation = network.newSubstation().setId("S").setCountry(Country.FR).add();
        for (int i = 1; i <= 3; i++) {
            VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VL" + i)
                .setNominalV(400.0 / i)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
            voltageLevel.getBusBreakerView().newBus().setId("B" + i).add();
        }
        substation.newThreeWindingsTransformer()
            .setId("TWT")
            .newLeg1().setVoltageLevel("VL1").setBus("B1").setConnectableBus("B1")
            .setR(1).setX(1).setG(0).setB(0).setRatedU(400).add()
            .newLeg2().setVoltageLevel("VL2").setBus("B2").setConnectableBus("B2")
            .setR(1).setX(1).setG(0).setB(0).setRatedU(200).add()
            .newLeg3().setVoltageLevel("VL3").setBus("B3").setConnectableBus("B3")
            .setR(1).setX(1).setG(0).setB(0).setRatedU(100).add()
            .add();
        return network;
    }

    @Test
    void shouldBeIndependentOfNodeAndConnectableOrder() {
        Network firstNetwork = createNodeBreakerNetwork(2, 3, 4, 5, 6, false);
        Network secondNetwork = createNodeBreakerNetwork(8, 6, 3, 7, 1, true);
        Bus firstBus = firstNetwork.getLoad("a-load").getTerminal().getBusView().getBus();
        Bus secondBus = secondNetwork.getLoad("a-load").getTerminal().getBusView().getBus();

        assertNotEquals(firstBus.getId(), secondBus.getId());
        assertEquals(Set.of("0-busbar", "00-load-busbar", "1-generated-load", "a-load", "z-generator"),
            getConnectedConnectableIds(firstBus));
        assertEquals(getConnectedConnectableIds(firstBus), getConnectedConnectableIds(secondBus));
        TerminalBasedBusRef firstRef = TerminalBasedBusRef.of(firstBus).orElseThrow();
        TerminalBasedBusRef secondRef = TerminalBasedBusRef.of(secondBus).orElseThrow();
        assertEquals(new TerminalBasedBusRef("a-load"), firstRef);
        assertEquals(firstRef, secondRef);
        assertEquals(firstBus, firstRef.resolve(firstNetwork, TopologyLevel.BUS_BRANCH).orElseThrow());
        assertEquals(secondBus, secondRef.resolve(secondNetwork, TopologyLevel.BUS_BRANCH).orElseThrow());

        Bus isolatedBus = firstNetwork.getLoad("02-isolated-generated").getTerminal().getBusView().getBus();
        assertTrue(TerminalBasedBusRef.of(isolatedBus).isEmpty());
        assertEquals(new TerminalBasedBusRef("00-isolated-busbar"), TerminalBasedBusRef.of(isolatedBus, BusbarSection.class::isInstance).orElseThrow());
    }

    @Test
    void shouldFollowAnchorWhenSwitchChangesBusMembership() {
        Network network = createNodeBreakerNetwork(2, 3, 4, 5, 6, false);
        Generator generator = network.getGenerator("z-generator");
        Load load = network.getLoad("a-load");
        Bus initialBus = load.getTerminal().getBusView().getBus();
        TerminalBasedBusRef initialRef = TerminalBasedBusRef.of(initialBus).orElseThrow();

        network.getSwitch("SW-GENERATOR-LOAD").setOpen(true);

        Bus generatorBus = generator.getTerminal().getBusView().getBus();
        Bus loadBus = load.getTerminal().getBusView().getBus();
        assertNotEquals(generatorBus.getId(), loadBus.getId());
        assertEquals(new TerminalBasedBusRef("z-generator"), TerminalBasedBusRef.of(generatorBus).orElseThrow());
        assertNotEquals(initialRef, TerminalBasedBusRef.of(generatorBus).orElseThrow());
        assertEquals(loadBus, initialRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow());
    }

    @Test
    void shouldBeIndependentOfConnectableOrderInBusBreakerTopology() {
        Network firstNetwork = createBusBreakerNetwork(false);
        Network secondNetwork = createBusBreakerNetwork(true);
        Bus firstBus = firstNetwork.getLoad("a-load").getTerminal().getBusView().getBus();
        Bus secondBus = secondNetwork.getLoad("a-load").getTerminal().getBusView().getBus();

        assertEquals(firstBus.getId(), secondBus.getId());
        assertEquals(getConnectedConnectableIds(firstBus), getConnectedConnectableIds(secondBus));
        assertEquals(Set.of("00-generated-load", "a-load", "z-generator"), getConnectedConnectableIds(firstBus));
        assertEquals(new TerminalBasedBusRef("a-load"), TerminalBasedBusRef.of(firstBus).orElseThrow());
        assertEquals(TerminalBasedBusRef.of(firstBus), TerminalBasedBusRef.of(secondBus));
    }

    @Test
    void shouldDistinguishAllThreeWindingsTransformerTerminals() {
        Network network = createThreeWindingsTransformerNetwork();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("TWT");
        for (ThreeSides side : ThreeSides.values()) {
            Terminal terminal = transformer.getTerminal(side);
            Bus bus = terminal.getBusView().getBus();
            TerminalBasedBusRef ref = TerminalBasedBusRef.of(bus).orElseThrow();
            assertEquals(new TerminalBasedBusRef("TWT", side, null), ref);
            assertEquals(bus, ref.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow());
        }
    }
}
