package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class ConnectFeedersToBusbarSectionsTest extends AbstractModificationTest {

    @Test
    void testConnectFeedersToBusbarSectionsWithCouplingDevice() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithCouplingDevices.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithCouplingDevices.xiidm"));

        NetworkModification modification = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(c -> !(c instanceof BusbarSection)).toList())
                .withBusbarSectionsToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(BusbarSection.class::isInstance).map(c -> (BusbarSection) c).toList())
                .withConnectCouplingDevices(true)
                .build();

        modification.apply(network);
        writeXmlTest(network, "/testNetworkNodeBreakerWithConnectFeedersToBusbarSectionsWithCouplingDevices.xiidm");

        // Check that nothing changes if we apply the modification a second time
        modification.apply(network);
        writeXmlTest(network, "/testNetworkNodeBreakerWithConnectFeedersToBusbarSectionsWithCouplingDevices.xiidm");
    }

    @Test
    void testConnectFeedersToBusbarSectionsWithoutCouplingDevicesAndOnlyLoads() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithCouplingDevices.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithCouplingDevices.xiidm"));

        NetworkModification modification = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(Load.class::isInstance).toList())
                .withBusbarSectionsToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(BusbarSection.class::isInstance).map(c -> (BusbarSection) c).toList())
                .withConnectCouplingDevices(false)
                .build();

        modification.apply(network);
        network.write("XIIDM", new java.util.Properties(), Path.of("/tmp/test.xiidm"));
        writeXmlTest(network, "/testNetwork3BusbarSectionsWithCouplingDeviceAndConnectedLoads.xiidm");

        // Check that nothing changes if we apply the modification a second time
        modification.apply(network);
        writeXmlTest(network, "/testNetwork3BusbarSectionsWithCouplingDeviceAndConnectedLoads.xiidm");
    }
}
