package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

public final class BusbarSectionTestNetworkFactory {

    private BusbarSectionTestNetworkFactory() {
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
        vl.getNodeBreakerView().setNodeCount(1);
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        return network;
    }

}
