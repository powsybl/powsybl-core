package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

public final class NetworkTest2Factory {

    private NetworkTest2Factory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().setNodeCount(3);
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        vl.newGenerator()
                .setId("G")
                .setNode(1)
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(10)
                .setMaxP(20)
                .setTargetP(20)
                .setTargetV(400)
                .setVoltageRegulatorOn(true)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("SW")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().setNodeCount(1);
        network.newLine()
                .setId("L")
                .setVoltageLevel1("VL")
                .setNode1(2)
                .setVoltageLevel2("VL2")
                .setNode2(0)
                .setR(1)
                .setX(1)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        return network;
    }

}
