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
    public void testComponentSetterGetter() {
        // Not implemented yet !
        TestUtil.notImplemented(bus::getSynchronousComponent);
        TestUtil.notImplemented(bus::getConnectedComponent);
        TestUtil.notImplemented(bus::isInMainConnectedComponent);
        TestUtil.notImplemented(bus::isInMainSynchronousComponent);
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
        createDangingLine(vl1, baseId + "1", baseName + "1", r, x, g, b, p0, q0, ucteXnodeCode, "busA");
        createDangingLine(vl3, baseId + "2", baseName + "2", r, x, g, b, p0, q0, ucteXnodeCode, "busC");
        createDangingLine(vl1, baseId + "3", baseName + "3", r, x, g, b, p0, q0, "", "busA");
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
        DanglingLine dl1 = createDangingLine(vl1, baseId + "1", baseName + "1", r, x, g, b, p0, q0, ucteXnodeCode, busId1);

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
        DanglingLine dl2 = createDangingLine(vl2, baseId + "2", baseName + "2", r, x, g, b, p0, q0, ucteXnodeCode, busId2);

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
    public void testBusViewSetterGetter() {
        final BusView busView = mergingView.getBusView();
        assertNotNull(busView);

        assertTrue(busView.getBuses().iterator().hasNext());
        busView.getBuses().forEach(b -> assertTrue(b instanceof AbstractAdapter));
        final Bus bus = busView.getBus("VLHV1_0");
        assertNotNull(bus);
        assertSame(mergingView, bus.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(busView::getConnectedComponents);
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

    private DanglingLine createDangingLine(VoltageLevel vl, String id, String name,
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
}
