package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

public final class MultipleExtensionsTestNetworkFactory {

    private MultipleExtensionsTestNetworkFactory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2017-11-17T12:00:00+01:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(20.0f)
                .setLowVoltageLimit(15.0f)
                .setHighVoltageLimit(25.0f)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("BUS")
                .add();
        Load load = vl.newLoad()
                .setId("LOAD")
                .setP0(0.0f)
                .setQ0(0.0f)
                .setBus("BUS")
                .setConnectableBus("BUS")
                .add();
        load.addExtension(LoadFooExt.class, new LoadFooExt(load));
        load.addExtension(LoadBarExt.class, new LoadBarExt(load));

        return network;
    }

}
