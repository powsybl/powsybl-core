package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import org.junit.Test;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public abstract class AbstractRemoteReactivePowerControlTest {

    public Network createNetwork() {
        Network network = Network.create("test", "test");
        Bus b1 = createBus(network, "b1_s", "b1", 1);
        Bus b2 = createBus(network, "b2_s", "b2", 1);
        Bus b3 = createBus(network, "b3_s", "b3", 1);
        Bus b4 = createBus(network, "b4_s", "b4", 1);
        createGenerator(b1, "g1", 2, 1);
        createGenerator(b4, "g4", 1, 1);
        createLoad(b2, "d2", 1, 1);
        createLoad(b3, "d3", 4, 1);
        createLine(network, b1, b4, "l14", 0.1f);
        createLine(network, b1, b2, "l12", 0.1f);
        createLine(network, b2, b3, "l23", 0.1f);
        createLine(network, b3, b4, "l34", 0.1f);
        createLine(network, b1, b3, "l13", 0.1f);
        return network;
    }

    //nominal v = 1
    protected static Bus createBus(Network network, String substationId, String id, double nominalV) {
        Substation s = network.getSubstation(substationId);
        if (s == null) {
            s = network.newSubstation()
                       .setId(substationId)
                       .setCountry(Country.FR)
                       .add();
        }
        VoltageLevel vl = s.newVoltageLevel()
                           .setId(id + "_vl")
                           .setNominalV(nominalV)
                           .setTopologyKind(TopologyKind.BUS_BREAKER)
                           .add();
        return vl.getBusBreakerView().newBus()
                 .setId(id)
                 .add();
    }

    //v=1
    protected static Generator createGenerator(Bus b, String id, double p, double v) {
        Generator g = b.getVoltageLevel()
                       .newGenerator()
                       .setId(id)
                       .setBus(b.getId())
                       .setConnectableBus(b.getId())
                       .setEnergySource(EnergySource.OTHER)
                       .setMinP(0)
                       .setMaxP(p)
                       .setTargetP(p)
                       .setTargetV(v)
                       .setVoltageRegulatorOn(true)
                       .add();
        g.getTerminal().setP(-p).setQ(0);
        return g;
    }

    protected static Load createLoad(Bus b, String id, double p, double q) {
        Load l = b.getVoltageLevel().newLoad()
                  .setId(id)
                  .setBus(b.getId())
                  .setConnectableBus(b.getId())
                  .setP0(p)
                  .setQ0(q)
                  .add();
        l.getTerminal().setP(p).setQ(q);
        return l;
    }

    protected static Line createLine(Network network, Bus b1, Bus b2, String id, double x) {
        return network.newLine()
                      .setId(id)
                      .setVoltageLevel1(b1.getVoltageLevel().getId())
                      .setBus1(b1.getId())
                      .setConnectableBus1(b1.getId())
                      .setVoltageLevel2(b2.getVoltageLevel().getId())
                      .setBus2(b2.getId())
                      .setConnectableBus2(b2.getId())
                      .setR(0)
                      .setX(x)
                      .setG1(0)
                      .setG2(0)
                      .setB1(0)
                      .setB2(0)
                      .add();
    }

    @Test
    public void test() {

        Network network = createNetwork();
        Generator g = network.getGenerator("g4");
        Line l = network.getLine("l34");

        g.newExtension(RemoteReactivePowerControlAdder.class);

        RemoteReactivePowerControl control = g.getExtension(RemoteReactivePowerControl.class);
    }
}
