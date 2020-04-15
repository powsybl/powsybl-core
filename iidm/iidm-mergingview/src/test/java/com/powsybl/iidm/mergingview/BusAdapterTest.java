/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.Network.BusBreakerView;
import com.powsybl.iidm.network.Network.BusView;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
        TestUtil.notImplemented(bus::getLines);
        TestUtil.notImplemented(bus::getLineStream);
        TestUtil.notImplemented(bus::getDanglingLines);
        TestUtil.notImplemented(bus::getDanglingLineStream);
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
