package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NodeBreakerFictitiousInjectionTest {

    @Test
    void testHasFictitiousP0AndFictitiousQ0() {
        Network network = NetworkTest1Factory.create();
        Bus bus = network.getVoltageLevel("voltageLevel1").getBusBreakerView().getBus("voltageLevel1_0");

        bus.setFictitiousP0(10);
        bus.setFictitiousQ0(20);
        VoltageLevelImpl vl = ((VoltageLevelImpl) network.getVoltageLevel("voltageLevel1"));
        assertTrue(vl.getNodeBreakerView().hasFictitiousP0());
        assertTrue(vl.getNodeBreakerView().hasFictitiousQ0());
        assertEquals(10, bus.getFictitiousP0());
        assertEquals(20, bus.getFictitiousQ0());

        bus.setFictitiousP0(0.0);
        bus.setFictitiousQ0(0.0);
        assertFalse(vl.getNodeBreakerView().hasFictitiousP0());
        assertFalse(vl.getNodeBreakerView().hasFictitiousQ0());
        assertEquals(0, bus.getFictitiousP0());
        assertEquals(0, bus.getFictitiousQ0());
    }
}
