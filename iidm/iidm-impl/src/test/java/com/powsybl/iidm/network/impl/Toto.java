package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class Toto {

    /**
     * <pre>
     *           LD        G
     *           |         |
     *           |    C    |
     *  BBS1 --------[-]-------- BBS2
     * </pre>
     */
    private Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(1)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("C")
                .setNode1(1)
                .setNode2(0)
                .setOpen(false)
                .setRetained(true)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D1")
                .setNode1(2)
                .setNode2(0)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D2")
                .setNode1(3)
                .setNode2(1)
                .setOpen(false)
                .add();
        vl.newLoad()
                .setId("LD")
                .setNode(2)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.newGenerator()
                .setId("G")
                .setNode(3)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(400)
                .setTargetP(1)
                .setTargetQ(0)
                .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        VoltageLevel vl = network.getVoltageLevel("VL");
        List<Bus> busesFromBusBreakerView = vl.getBusBreakerView().getBusStream().collect(Collectors.toList());
        List<Bus> busesFromBusView = vl.getBusView().getBusStream().collect(Collectors.toList());
        assertEquals(2, busesFromBusBreakerView.size());
        assertEquals(1, busesFromBusView.size());
        network.getSwitch("C").setOpen(true);
        List<Bus> busesFromBusBreakerView2 = vl.getBusBreakerView().getBusStream().collect(Collectors.toList());
        List<Bus> busesFromBusView2 = vl.getBusView().getBusStream().collect(Collectors.toList());
        assertEquals(2, busesFromBusBreakerView2.size());
        assertEquals(2, busesFromBusView2.size());
        assertSame(busesFromBusBreakerView.get(0), busesFromBusBreakerView2.get(0));
        assertSame(busesFromBusBreakerView.get(1), busesFromBusBreakerView2.get(1));
    }
}
