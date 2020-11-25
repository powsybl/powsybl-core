/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Network.BusBreakerView;
import com.powsybl.iidm.network.Network.BusView;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collection;

import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusAdapterTest {
    private MergingView mergingView;
    private Bus bus;

    @Before
    public void setup() {
        mergingView = MergingView.create("BatteryAdapterTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
        mergingView.merge(HvdcTestNetwork.createVsc());

        bus = mergingView.getBusBreakerView().getBus("NGEN");
        assertNotNull(bus);
        assertTrue(bus instanceof BusAdapter);
        assertSame(mergingView, bus.getNetwork());
    }

    @Test
    public void testSetterGetter() {
        assertNotNull(bus.getVoltageLevel());
        bus.setV(0.0d);
        assertEquals(0.0d, bus.getV(), 0.0d);
        bus.setAngle(0.0d);
        assertEquals(0.0d, bus.getAngle(), 0.0d);
        assertEquals(0.0d, bus.getP(), 0.0d);
        assertEquals(0.0d, bus.getQ(), 0.0d);
        assertTrue(bus.getTwoWindingsTransformers().iterator().hasNext());
        assertEquals(1, bus.getTwoWindingsTransformerStream().count());
        assertFalse(bus.getThreeWindingsTransformers().iterator().hasNext());
        assertEquals(0, bus.getThreeWindingsTransformerStream().count());
        assertTrue(bus.getGenerators().iterator().hasNext());
        assertEquals(1, bus.getGeneratorStream().count());
        assertFalse(bus.getBatteries().iterator().hasNext());
        assertEquals(0, bus.getBatteryStream().count());
        assertFalse(bus.getLoads().iterator().hasNext());
        assertEquals(0, bus.getLoadStream().count());
        assertFalse(bus.getShuntCompensators().iterator().hasNext());
        assertEquals(0, bus.getShuntCompensatorStream().count());
        assertFalse(bus.getStaticVarCompensators().iterator().hasNext());
        assertEquals(0, bus.getStaticVarCompensatorStream().count());
        assertFalse(bus.getLccConverterStations().iterator().hasNext());
        assertEquals(0, bus.getLccConverterStationStream().count());
        assertFalse(bus.getVscConverterStations().iterator().hasNext());
        assertEquals(0, bus.getVscConverterStationStream().count());

        // Not implemented yet !
        TestUtil.notImplemented(bus::getConnectedTerminalCount);
    }

    @Test
    public void testSynchronousComponentSetterGetter() {
        MergingView view = MergingView.create("testSynchronousComponentSetterGetter", "iidm");
        Network network = HvdcTestNetwork.createVsc();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        vl1.getBusBreakerView()
                .newBus()
                .setId("disconnected")
                .add();
        view.merge(network);
        HvdcLine l = view.getHvdcLine("L");
        assertNotNull(l);
        VscConverterStation cs1 = view.getVscConverterStation("C1");
        VscConverterStation cs2 = view.getVscConverterStation("C2");
        cs1.setLossFactor(0.022f);
        cs1.setVoltageSetpoint(406.0);
        cs2.setReactivePowerSetpoint(124.0);
        cs2.setVoltageSetpoint(405);
        cs2.setVoltageRegulatorOn(true);
        Bus bus1 = l.getConverterStation1().getTerminal().getBusView().getBus();
        Bus bus2 = l.getConverterStation2().getTerminal().getBusView().getBus();
        assertTrue(bus1.isInMainConnectedComponent());
        assertTrue(bus2.isInMainConnectedComponent());
        assertTrue(bus1.isInMainSynchronousComponent());
        assertFalse(bus2.isInMainSynchronousComponent());
        int num1 = bus1.getSynchronousComponent().getNum();
        int num2 = bus2.getSynchronousComponent().getNum();
        assertNotEquals(num1, num2);
        Bus disconnected = view.getBusBreakerView().getBus("disconnected");
        assertNull(disconnected.getConnectedComponent());
        assertNull(disconnected.getSynchronousComponent());
        assertFalse(disconnected.isInMainConnectedComponent());
        assertFalse(disconnected.isInMainSynchronousComponent());
    }

    @Test
    public void testGetMergedLine() {
        // Networks creation
        Network network1 = NoEquipmentNetworkFactory.create();
        Network network2 = NetworkFactory.findDefault().createNetwork("test2", "test");
        Substation s2 = network2.newSubstation()
                .setId("sub2")
                .setCountry(Country.FR)
                .setTso("RTE")
                .add();
        VoltageLevel vl3 = s2.newVoltageLevel()
                .setId("vl3")
                .setName("vl3")
                .setNominalV(440.0)
                .setHighVoltageLimit(400.0)
                .setLowVoltageLimit(200.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("busC")
                .setName("busC")
                .add();

        VoltageLevel vl1 = network1.getVoltageLevel("vl1");

        double r = 10.0;
        double x = 20.0;
        double g = 30.0;
        double b = 40.0;
        double p0 = 50.0;
        double q0 = 60.0;
        String baseId = "DL";
        String baseName = "DanglingLine";
        String ucteXnodeCode = "code";
        createDanglingLine(vl1, baseId + "1", baseName + "1", r, x, g, b, p0, q0, ucteXnodeCode, "busA");
        createDanglingLine(vl3, baseId + "2", baseName + "2", r, x, g, b, p0, q0, ucteXnodeCode, "busC");
        createDanglingLine(vl1, baseId + "3", baseName + "3", r, x, g, b, p0, q0, "", "busA");
        network1.newLine()
                .setId("L")
                .setVoltageLevel1("vl1")
                .setBus1("busA")
                .setConnectableBus1("busA")
                .setVoltageLevel2("vl2")
                .setBus2("busB")
                .setConnectableBus2("busB")
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();

        // Check that dangling lines are present in networks and that merged line doesn't exist
        Bus busA = network1.getBusBreakerView().getBus("busA");
        assertFalse(busA.getDanglingLineStream().noneMatch(dl -> "DL1".equals(dl.getId())));
        assertFalse(StreamSupport.stream(busA.getDanglingLines().spliterator(), false).noneMatch(dl -> "DL1".equals(dl.getId())));
        assertTrue(busA.getLineStream().noneMatch(l -> "DL1 + DL2".equals(l.getId())));
        assertTrue(StreamSupport.stream(busA.getLines().spliterator(), false).noneMatch(l -> "DL1 + DL2".equals(l.getId())));

        Bus busC = network2.getBusBreakerView().getBus("busC");
        assertFalse(busC.getDanglingLineStream().noneMatch(dl -> "DL2".equals(dl.getId())));
        assertFalse(StreamSupport.stream(busC.getDanglingLines().spliterator(), false).noneMatch(dl -> "DL2".equals(dl.getId())));
        assertTrue(busC.getLineStream().noneMatch(l -> "DL1 + DL2".equals(l.getId())));
        assertTrue(StreamSupport.stream(busC.getLines().spliterator(), false).noneMatch(l -> "DL1 + DL2".equals(l.getId())));

        // Merge networks
        MergingView mergingView = MergingView.create("merge", "test");
        mergingView.merge(network1, network2);

        // Check that methods returning lines and dangling lines return adapters (wrapped objects)
        Bus mergedBusA = mergingView.getBusBreakerView().getBus("busA");

        assertTrue(mergedBusA.getLineStream().allMatch(l -> l instanceof LineAdapter || l instanceof MergedLine));
        assertTrue(mergedBusA.getDanglingLineStream().allMatch(dl -> dl instanceof DanglingLineAdapter));

        // Check that merged dangling lines are not present anymore and that merged line exist
        assertTrue(mergedBusA.getDanglingLineStream().noneMatch(dl -> "DL1".equals(dl.getId())));
        assertTrue(StreamSupport.stream(mergedBusA.getDanglingLines().spliterator(), false).noneMatch(dl -> "DL1".equals(dl.getId())));
        assertFalse(mergedBusA.getLineStream().noneMatch(l -> "DL1 + DL2".equals(l.getId())));
        assertFalse(StreamSupport.stream(mergedBusA.getLines().spliterator(), false).noneMatch(l -> "DL1 + DL2".equals(l.getId())));

        Bus mergedBusC = mergingView.getBusBreakerView().getBus("busC");
        assertTrue(mergedBusC.getDanglingLineStream().noneMatch(dl -> "DL2".equals(dl.getId())));
        assertTrue(StreamSupport.stream(mergedBusC.getDanglingLines().spliterator(), false).noneMatch(dl -> "DL2".equals(dl.getId())));
        assertFalse(mergedBusC.getLineStream().noneMatch(l -> "DL1 + DL2".equals(l.getId())));
        assertFalse(StreamSupport.stream(mergedBusC.getLines().spliterator(), false).noneMatch(l -> "DL1 + DL2".equals(l.getId())));
    }

    @Test
    public void testComponentVisitor() {
        final Network noEquipNetwork = NoEquipmentNetworkFactory.create();
        final String vlId1 = "vl1";
        final String vlId2 = "vl2";
        VoltageLevel vl1 = noEquipNetwork.getVoltageLevel(vlId1);
        VoltageLevel vl2 = noEquipNetwork.getVoltageLevel(vlId2);
        final String busId1 = "busA";
        final String busId2 = "busB";

        double r = 10.0;
        double x = 20.0;
        double g = 30.0;
        double b = 40.0;
        double p0 = 50.0;
        double q0 = 60.0;
        String baseId = "DL";
        String baseName = "DanglingLine";
        String ucteXnodeCode = "code";
        DanglingLine dl1 = createDanglingLine(vl1, baseId + "1", baseName + "1", r, x, g, b, p0, q0, ucteXnodeCode, busId1);

        // Create MergingView with Network with one DanglingLine
        MergingView view = MergingView.create("testComponentVisitor", "test");
        view.merge(noEquipNetwork);
        // Get BusAdapter from BusA
        Bus busA = view.getVoltageLevel(vlId1).getBusBreakerView().getBus(busId1);
        // Mock TopologyVisitorAdapter
        TopologyVisitor visitor1 = Mockito.mock(TopologyVisitor.class);
        // Visit
        busA.visitConnectedEquipments(visitor1);
        // Check DanglingLine is visited
        Mockito.verify(visitor1, Mockito.times(1)).visitDanglingLine(dl1);
        busA.visitConnectedOrConnectableEquipments(visitor1);
        // Check DanglingLine is visited
        Mockito.verify(visitor1, Mockito.times(2)).visitDanglingLine(dl1);
        // Check no line is visited
        Mockito.verify(visitor1, Mockito.never()).visitLine(Mockito.any(Line.class), Mockito.any(Branch.Side.class));

        // Add second DanglingLine -> Creation of MergedLine
        DanglingLine dl2 = createDanglingLine(vl2, baseId + "2", baseName + "2", r, x, g, b, p0, q0, ucteXnodeCode, busId2);

        // Mock TopologyVisitor
        TopologyVisitor visitor2 = Mockito.mock(TopologyVisitor.class);
        // Visit
        busA.visitConnectedEquipments(visitor2);
        // Check MergedLine is visited
        Mockito.verify(visitor2, Mockito.times(1)).visitLine(Mockito.any(Line.class), Mockito.any(Branch.Side.class));
        busA.visitConnectedOrConnectableEquipments(visitor2);
        // Check MergedLine is visited
        Mockito.verify(visitor2, Mockito.times(2)).visitLine(Mockito.any(Line.class), Mockito.any(Branch.Side.class));
        // Check no DanglingLine is visited
        Mockito.verify(visitor2, Mockito.never()).visitDanglingLine(dl1);
        Mockito.verify(visitor2, Mockito.never()).visitDanglingLine(dl2);
    }

    @Test
    public void testConnectedComponentSetterGetter() {
        Network network1 = createGeneratorNetwork();
        Network network2 = createLoadNetwork();

        Load load = network2.getLoad("LOAD");

        VoltageLevel vl1 = network1.getVoltageLevel("S1_VL");
        Bus b1 = vl1.getBusBreakerView().getBus("B1");
        Bus b2 = vl1.getBusBreakerView().getBus("B2");

        VoltageLevel vl2 = network2.getVoltageLevel("S2_VL");
        Bus b3 = vl2.getBusBreakerView().getBus("B3");

        assertTrue(b1.isInMainConnectedComponent());
        assertTrue(b2.isInMainConnectedComponent());
        assertTrue(b3.isInMainConnectedComponent());
        assertEquals(0, b1.getConnectedComponent().getNum());
        assertEquals(0, b2.getConnectedComponent().getNum());

        // Merge
        MergingView view = MergingView.create("MergingView", "test");
        // + Network1
        view.merge(network1);
        vl1 = view.getVoltageLevel("S1_VL");
        b1 = vl1.getBusBreakerView().getBus("B1");
        b2 = vl1.getBusBreakerView().getBus("B2");

        assertTrue(b1.isInMainConnectedComponent());
        assertTrue(b2.isInMainConnectedComponent());
    }

    private static Network createGeneratorNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("WithGen", "test");

        Substation substation = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = substation.newVoltageLevel()
                .setId("S1_VL")
                .setNominalV(100.0)
                .setLowVoltageLimit(80.0)
                .setHighVoltageLimit(120.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl.newGenerator()
                .setId("GEN")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(100.0)
                .setTargetP(50.0)
                .setTargetQ(30.0)
                .setBus("B1")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        network.newLine()
                .setId("L")
                .setVoltageLevel1("S1_VL")
                .setBus1("B1")
                .setVoltageLevel2("S1_VL")
                .setBus2("B2")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        vl.newDanglingLine()
                .setId("S2_DL")
                .setBus("B2")
                .setUcteXnodeCode("XNode")
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .add();
        return network;
    }

    private static Network createLoadNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("WithLoad", "test");

        Substation substation = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = substation.newVoltageLevel()
                .setId("S2_VL")
                .setNominalV(100.0)
                .setLowVoltageLimit(80.0)
                .setHighVoltageLimit(120.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B3")
                .add();
        vl.newLoad()
                .setId("LOAD")
                .setConnectableBus("B3")
                .setBus("B3")
                .setP0(100.0)
                .setQ0(1.0)
                .add();
        vl.newDanglingLine()
                .setId("S2_DL")
                .setBus("B3")
                .setUcteXnodeCode("XNode")
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .add();
        return network;
    }

    private DanglingLine createDanglingLine(VoltageLevel vl, String id, String name,
                                            double r, double x, double g, double b, double p0, double q0,
                                            String ucteXnodeCode, String busId)  {
        return vl.newDanglingLine()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setP0(p0)
                .setQ0(q0)
                .setUcteXnodeCode(ucteXnodeCode)
                .setBus(busId)
                .setConnectableBus(busId)
                .add();
    }

    @Test
    public void testBusViewSetterGetter() {
        final BusView busView = mergingView.getBusView();
        assertNotNull(busView);

        assertTrue(busView.getBuses().iterator().hasNext());
        busView.getBuses().forEach(b -> assertTrue(b instanceof AbstractAdapter));
        final Bus bus = busView.getBus("VLHV1_0");
        assertNotNull(bus);
        assertSame(mergingView, bus.getNetwork());

        Collection<Component> components = busView.getConnectedComponents();
        assertTrue(components.iterator().hasNext());
    }

    @Test
    public void testBusBreakerViewSetterGetter() {
        final BusBreakerView busBreakerView = mergingView.getBusBreakerView();
        assertNotNull(busBreakerView);

        assertTrue(busBreakerView.getBuses().iterator().hasNext());
        busBreakerView.getBuses().forEach(b -> assertTrue(b instanceof AbstractAdapter));
        assertTrue(busBreakerView.getSwitches().iterator().hasNext());
        busBreakerView.getSwitches().forEach(b -> assertTrue(b instanceof AbstractAdapter));
        assertEquals(1, busBreakerView.getSwitchCount());
    }

    @Test
    public void mainConnectedComponentWithSwitchTest() {
        // TODO: delete this test when TCK tests merging view
        Network network = Network.create("test_mcc", "test");

        Substation s1 = network.newSubstation()
                .setId("A")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("B")
                .setNominalV(225.0)
                .setLowVoltageLimit(0.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("C")
                .setNode(0)
                .add();
        vl1.getNodeBreakerView().newSwitch()
                .setId("D")
                .setKind(SwitchKind.DISCONNECTOR)
                .setRetained(false)
                .setOpen(false)
                .setNode1(0)
                .setNode2(1)
                .add();
        vl1.getNodeBreakerView().newSwitch()
                .setId("E")
                .setKind(SwitchKind.BREAKER)
                .setRetained(false)
                .setOpen(false)
                .setNode1(1)
                .setNode2(2)
                .add();

        Substation s2 = network.newSubstation()
                .setId("F")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("G")
                .setNominalV(225.0)
                .setLowVoltageLimit(0.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("H")
                .setNode(0)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("I")
                .setNode(1)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("J")
                .setKind(SwitchKind.DISCONNECTOR)
                .setRetained(true)
                .setOpen(false)
                .setNode1(0)
                .setNode2(2)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("K")
                .setKind(SwitchKind.DISCONNECTOR)
                .setRetained(true)
                .setOpen(false)
                .setNode1(1)
                .setNode2(3)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("L")
                .setKind(SwitchKind.BREAKER)
                .setRetained(true)
                .setOpen(false)
                .setNode1(2)
                .setNode2(3)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("M")
                .setKind(SwitchKind.BREAKER)
                .setRetained(false)
                .setOpen(false)
                .setNode1(0)
                .setNode2(4)
                .add();

        network.newLine()
                .setId("N")
                .setR(0.001)
                .setX(0.1)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setVoltageLevel1("B")
                .setNode1(2)
                .setVoltageLevel2("G")
                .setNode2(4)
                .add();

        network.getBusView().getBuses().forEach(b -> {
            if (b.getVoltageLevel() == vl1) {
                b.setV(230.0).setAngle(0.5);
            } else {
                b.setV(220.0).setAngle(0.7);
            }
        });

        MergingView mergingView = MergingView.create("merged", "test");
        mergingView.merge(network);

        assertEquals(2, mergingView.getBusView().getBusStream().count());
        for (Bus b : mergingView.getBusView().getBuses()) {
            assertTrue(b.isInMainConnectedComponent());
        }

        assertEquals(5, mergingView.getBusBreakerView().getBusStream().count());
        for (Bus b : mergingView.getBusBreakerView().getBuses()) {
            assertTrue(b.isInMainConnectedComponent());
            if (b.getVoltageLevel().getId().equals(vl1.getId())) {
                assertEquals(230.0, b.getV(), 0.0);
                assertEquals(0.5, b.getAngle(), 0.0);
            } else {
                assertEquals(220.0, b.getV(), 0.0);
                assertEquals(0.7, b.getAngle(), 0.0);
            }
        }
    }
}
