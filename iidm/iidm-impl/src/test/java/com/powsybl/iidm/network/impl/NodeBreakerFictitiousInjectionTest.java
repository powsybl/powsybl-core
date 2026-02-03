package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeBreakerFictitiousInjectionTest {

    @Test
    void testHasFictitiousP0AndFictitiousQ0() {
        Network network = NetworkTest1Factory.create();
        Bus bus = network.getVoltageLevel("voltageLevel1").getBusBreakerView().getBus("voltageLevel1_0");
        bus.setFictitiousP0(10);
        bus.setFictitiousQ0(20);
        assertTrue(((VoltageLevelImpl) network.getVoltageLevel("voltageLevel1")).getNodeBreakerView().hasFictitiousP0());
        assertTrue(((VoltageLevelImpl) network.getVoltageLevel("voltageLevel1")).getNodeBreakerView().hasFictitiousQ0());
        bus.setFictitiousP0(0.0);
        bus.setFictitiousQ0(0.0);
        assertFalse(((VoltageLevelImpl) network.getVoltageLevel("voltageLevel1")).getNodeBreakerView().hasFictitiousP0());
        assertFalse(((VoltageLevelImpl) network.getVoltageLevel("voltageLevel1")).getNodeBreakerView().hasFictitiousP0());
    }
}
