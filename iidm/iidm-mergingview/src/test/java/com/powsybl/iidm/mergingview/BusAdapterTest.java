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
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

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
        Network network1 = createGeneratorNetwork();
        Network network2 = createLoadNetwork();

        Generator generator = network1.getGenerator("GEN");
        Load load = network2.getLoad("LOAD");

        VoltageLevel vl1 = network1.getVoltageLevel("S1_VL");
        Bus b1 = vl1.getBusBreakerView().getBus("B1");
        Bus b2 = vl1.getBusBreakerView().getBus("B2");

        VoltageLevel vl2 = network2.getVoltageLevel("S2_VL");
        Bus b3 = vl2.getBusBreakerView().getBus("B3");

        assertTrue(b1.isInMainConnectedComponent());
        assertTrue(b2.isInMainConnectedComponent());
        assertFalse(b3.isInMainConnectedComponent());
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
}
