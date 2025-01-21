/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public abstract class AbstractBusBreakerTest {

    private static Network createTestNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region1")
                .add();
        VoltageLevel vl1 = s.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("BR1")
                .setBus1("B1")
                .setBus2("B2")
                .setOpen(false)
                .add();

        vl1.newGenerator()
                .setId("G")
                .setBus("B1")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();
        vl1.newLoad()
                .setId("LD1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        vl1.newLoad()
                .setId("LD2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B21")
                .add();
        vl2.newGenerator()
                .setId("G2")
                .setBus("B21")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();

        network.newLine()
                .setId("L1")
                .setVoltageLevel1("VL1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setBus2("B21")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        return network;
    }

    @Test
    public void testSetterGetter() {
        Network network = Network.create("test", "test");
        Substation substation = network.newSubstation()
                                    .setCountry(Country.AF)
                                    .setTso("tso")
                                    .setName("sub")
                                    .setId("subId")
                                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                                        .setTopologyKind(TopologyKind.BUS_BREAKER)
                                        .setId("bbVL")
                                        .setName("bbVL_name")
                                        .setNominalV(200.0f)
                                    .add();
        // ConfiguredBus
        Bus bus = voltageLevel.getBusBreakerView()
                    .newBus()
                    .setName("bus1Name")
                    .setId("bus1")
                .add();

        assertNotNull(network.getBusBreakerView().getBus("bus1"));
        assertEquals("bus1", network.getBusBreakerView().getBus("bus1").getId());
        assertEquals("bus1", bus.getId());
        assertEquals("bus1Name", bus.getOptionalName().orElse(null));
        assertEquals("bus1Name", bus.getNameOrId());

        LccConverterStation lccConverterStation = voltageLevel.newLccConverterStation()
                                                    .setId("lcc")
                                                    .setName("lcc")
                                                    .setBus("bus1")
                                                    .setLossFactor(0.011f)
                                                    .setPowerFactor(0.5f)
                                                    .setConnectableBus("bus1")
                                                .add();
        VscConverterStation vscConverterStation = voltageLevel.newVscConverterStation()
                                                    .setId("vsc")
                                                    .setName("vsc")
                                                    .setBus("bus1")
                                                    .setLossFactor(0.011f)
                                                    .setVoltageRegulatorOn(false)
                                                    .setReactivePowerSetpoint(1.0)
                                                    .setConnectableBus("bus1")
                                                .add();
        assertEquals(HvdcConverterStation.HvdcType.LCC, lccConverterStation.getHvdcType());
        assertEquals(HvdcConverterStation.HvdcType.VSC, vscConverterStation.getHvdcType());
        double p1 = 1.0;
        double q1 = 2.0;
        double p2 = 10.0;
        double q2 = 20.0;
        lccConverterStation.getTerminal().setP(p1);
        lccConverterStation.getTerminal().setQ(q1);
        vscConverterStation.getTerminal().setP(p2);
        vscConverterStation.getTerminal().setQ(q2);

        assertSame(voltageLevel, bus.getVoltageLevel());
        assertSame(network, bus.getNetwork());
        try {
            bus.setV(-1.0);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        bus.setV(200.0);
        assertEquals(200.0, bus.getV(), 0.0);
        bus.setAngle(30.0);
        assertEquals(30.0, bus.getAngle(), 0.0);

        assertEquals(p1 + p2, bus.getP(), 0.0);
        assertEquals(q1 + q2, bus.getQ(), 0.0);

        assertEquals(0.0, bus.getFictitiousP0(), 0.0);
        assertEquals(0.0, bus.getFictitiousQ0(), 0.0);
        bus.setFictitiousP0(1.0).setFictitiousQ0(2.0);
        assertEquals(1.0, bus.getFictitiousP0(), 0.0);
        assertEquals(2.0, bus.getFictitiousQ0(), 0.0);
        Bus busViewBus = bus.getConnectedTerminalStream()
                .map(t -> t.getBusView().getBus())
                .filter(Objects::nonNull).findFirst().orElseThrow(IllegalStateException::new);
        assertEquals(1.0, busViewBus.getFictitiousP0(), 0.0);
        assertEquals(2.0, busViewBus.getFictitiousQ0(), 0.0);
        busViewBus.setFictitiousP0(3.0).setFictitiousQ0(4.0);
        assertEquals(3.0, bus.getFictitiousP0(), 0.0);
        assertEquals(4.0, bus.getFictitiousQ0(), 0.0);
    }

    @Test
    public void testConnectedTerminals() {
        Network network = createTestNetwork();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        Terminal gt = network.getGenerator("G").getTerminal();
        Terminal ld1t = network.getLoad("LD1").getTerminal();
        Terminal ld2t = network.getLoad("LD2").getTerminal();
        Terminal l1t = network.getLine("L1").getTerminal1();

        assertTrue(gt.isConnected());
        assertTrue(ld1t.isConnected());
        assertTrue(ld2t.isConnected());
        assertTrue(l1t.isConnected());

        assertEquals(1, vl1.getBusView().getBusStream().count());

        assertEquals(gt.getBusView().getBus(), gt.getBusView().getConnectableBus());
        assertEquals(ld1t.getBusView().getBus(), ld1t.getBusView().getConnectableBus());
        assertEquals(ld2t.getBusView().getBus(), ld2t.getBusView().getConnectableBus());
        assertEquals(l1t.getBusView().getBus(), l1t.getBusView().getConnectableBus());

        assertEquals(4, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(4, Lists.newArrayList(vl1.getBusView().getBus("VL1_0").getConnectedTerminals()).size());
    }

    @Test
    public void testDisconnectConnect() {
        Network network = createTestNetwork();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        Terminal gt = network.getGenerator("G").getTerminal();
        Terminal l1t = network.getLine("L1").getTerminal1();

        assertTrue(gt.disconnect());
        assertFalse(gt.isConnected());
        assertNull(gt.getBusView().getBus());
        assertNotNull(gt.getBusView().getConnectableBus());
        assertEquals(vl1.getBusView().getBus("VL1_0"), gt.getBusView().getConnectableBus());
        assertEquals(3, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(3, Lists.newArrayList(vl1.getBusView().getBus("VL1_0").getConnectedTerminals()).size());

        assertTrue(l1t.disconnect());
        assertFalse(l1t.isConnected());
        assertEquals(1, vl1.getBusView().getBusStream().count());
        assertNull(l1t.getBusView().getBus());
        assertNotNull(l1t.getBusView().getConnectableBus());
        assertEquals(vl1.getBusView().getBus("VL1_0"), l1t.getBusView().getConnectableBus());
        assertTrue(l1t.connect());
        assertTrue(l1t.isConnected());

        assertTrue(gt.connect());
        assertTrue(gt.isConnected());
        assertEquals(vl1.getBusView().getBus("VL1_0"), gt.getBusView().getConnectableBus());

        assertEquals(4, vl1.getBusView().getBus("VL1_0").getConnectedTerminalCount());
        assertEquals(4, Lists.newArrayList(vl1.getBusView().getBus("VL1_0").getConnectedTerminals()).size());
    }

    @Test
    public void testNodeBreakerNonSupportedMethods() {
        Network network = createTestNetwork();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel.NodeBreakerView nodeBreakerView = vl1.getNodeBreakerView();
        assertThrows(PowsyblException.class, () -> nodeBreakerView.getSwitchStream(0), "Not supported in a bus breaker topology");
        assertThrows(PowsyblException.class, () -> nodeBreakerView.getSwitches(1), "Not supported in a bus breaker topology");
        assertThrows(PowsyblException.class, () -> nodeBreakerView.getNodeInternalConnectedToStream(2), "Not supported in a bus breaker topology");
        assertThrows(PowsyblException.class, () -> nodeBreakerView.getNodesInternalConnectedTo(3), "Not supported in a bus breaker topology");
    }

    @Test
    void testFictitiousP0AndFictitiousQ0ForInvalidatedBus() {
        Network network = createTestNetwork();
        Bus bus = network.getVoltageLevel("VL1").getBusView().getBus("VL1_0");
        network.getSwitch("BR1").setOpen(true);
        assertThrows(PowsyblException.class, bus::getFictitiousP0, "Bus has been invalidated");
        assertThrows(PowsyblException.class, bus::getFictitiousQ0, "Bus has been invalidated");
    }
}
