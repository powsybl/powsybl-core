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
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Before;
import org.junit.Test;

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
    public void testSynchronousComponentSetterGetter() {
        MergingView view = MergingView.create("testSynchronousComponentSetterGetter", "iidm");
        view.merge(HvdcTestNetwork.createVsc());
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
    }

    @Test
    public void testConnectedComponentSetterGetter() {
        MergingView view = MergingView.create("testConnectedComponentSetterGetter", "iidm");
        view.merge(NetworkTest1Factory.create());
        VoltageLevel voltageLevel1 = view.getVoltageLevel("voltageLevel1");
        VoltageLevel.NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();
        BusbarSection voltageLevel1BusbarSection1 = topology1.getBusbarSection("voltageLevel1BusbarSection1");
        BusbarSection voltageLevel1BusbarSection2 = topology1.getBusbarSection("voltageLevel1BusbarSection2");
        Load load1 = view.getLoad("load1");
        Generator generator1 = view.getGenerator("generator1");
        Bus busCalc1 = voltageLevel1BusbarSection1.getTerminal().getBusBreakerView().getBus();
        Bus busCalc2 = voltageLevel1BusbarSection2.getTerminal().getBusBreakerView().getBus();
        assertSame(busCalc1, load1.getTerminal().getBusBreakerView().getBus());
        assertSame(busCalc2, generator1.getTerminal().getBusBreakerView().getBus());

        assertTrue(busCalc1.isInMainConnectedComponent());
        assertTrue(busCalc2.isInMainConnectedComponent());
        assertEquals(0, busCalc1.getConnectedComponent().getNum());
        assertEquals(0, busCalc2.getConnectedComponent().getNum());
    }

    @Test
    public void testComponentVisitor() {
        // Not implemented yet !
        TestUtil.notImplemented(() -> bus.visitConnectedEquipments(null));
        TestUtil.notImplemented(() -> bus.visitConnectedOrConnectableEquipments(null));
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
}
