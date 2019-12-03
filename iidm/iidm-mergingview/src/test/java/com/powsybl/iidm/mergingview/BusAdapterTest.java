/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network.BusBreakerView;
import com.powsybl.iidm.network.Network.BusView;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.apache.commons.lang3.NotImplementedException;
import org.assertj.core.api.Assertions;
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
        assertFalse(bus.getThreeWindingsTransformers().iterator().hasNext());
        assertTrue(bus.getGenerators().iterator().hasNext());
        assertFalse(bus.getBatteries().iterator().hasNext());
        assertFalse(bus.getLoads().iterator().hasNext());
        assertFalse(bus.getShuntCompensators().iterator().hasNext());
        assertFalse(bus.getStaticVarCompensators().iterator().hasNext());
        assertFalse(bus.getLccConverterStations().iterator().hasNext());
        assertFalse(bus.getVscConverterStations().iterator().hasNext());

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
    public void testBusViewSetterGetter() {
        final BusView busView = mergingView.getBusView();
        assertNotNull(busView);
        assertTrue(busView instanceof NetworkBusViewAdapter);

        assertTrue(busView.getBuses().iterator().hasNext());
        busView.getBuses().forEach(b -> assertTrue(b instanceof AbstractAdapter<?>));
        assertEquals(2, busView.getConnectedComponents().size());
        busView.getConnectedComponents().forEach(b -> assertTrue(b instanceof AbstractAdapter<?>));
        assertNotNull(busView.getBus("VLHV1_0"));
    }

    @Test
    public void testBusBreakerViewSetterGetter() {
        final BusBreakerView busBreakerView = mergingView.getBusBreakerView();
        assertNotNull(busBreakerView);
        assertTrue(busBreakerView instanceof NetworkBusBreakerViewAdapter);

        assertTrue(busBreakerView.getBuses().iterator().hasNext());
        busBreakerView.getBuses().forEach(b -> assertTrue(b instanceof AbstractAdapter<?>));
        assertTrue(busBreakerView.getSwitches().iterator().hasNext());
        busBreakerView.getSwitches().forEach(b -> assertTrue(b instanceof AbstractAdapter<?>));
        assertEquals(1, busBreakerView.getSwitchCount());
    }

    @Test
    public void testNewDefaultMethodsOnBusApi() {
        Assertions.assertThatThrownBy(bus::getConnectedTerminalStream).isInstanceOf(NotImplementedException.class);
        Assertions.assertThatThrownBy(bus::getConnectedTerminals).isInstanceOf(NotImplementedException.class);
        Assertions.assertThatThrownBy(bus::getNodes).isInstanceOf(NotImplementedException.class);
        Assertions.assertThatThrownBy(bus::getNodeCount).isInstanceOf(NotImplementedException.class);
    }
}
